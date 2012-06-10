/*
 * Copyright (C) 2007-2008 JVending Masa
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jvending.masa.plugin.apkbuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.IOUtil;

import com.android.sdklib.build.ApkBuilder;
import com.android.sdklib.build.ApkCreationException;
import com.android.sdklib.build.DuplicateFileException;
import com.android.sdklib.build.SealedApkException;

/**
 * @goal build
 * @phase package
 * @description
 */
public class ApkBuilderMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     */
    public MavenProject project;

    /**
     * @parameter expression="${session}"
     */
    public MavenSession session;

    /**
     * Maven ProjectHelper.
     * 
     * @component
     * @readonly
     */
    private MavenProjectHelper projectHelper;

    /**
     * Sets the debug mode. In debug mode, when native libraries are present, the packaging
     * will also include one or more copies of gdbserver in the final APK file.
     *
     * These are used for debugging native code, to ensure that gdbserver is accessible to the
     * application.
     *
     * There will be one version of gdbserver for each ABI supported by the application.
     *
     * the gbdserver files are placed in the libs/abi/ folders automatically by the NDK.
     *
     * @parameter
     */
    private boolean debugMode;

    /**
     * Root folder containing native libraries to include in the application package.
     * 
     * @parameter
     */
    private NativeLibraries nativeLibraries;

    /**
     * @parameter
     */
    private boolean verboseMode;

    /**
     * @parameter
     */
    public PrivateKeyInfo privatekeyInfo;

    /**
     * @parameter
     */
    public X509Cert certificate;

    /**
     * Signed by open-source platform keys
     * 
     * @parameter
     */
    public boolean isAospSigned;

    /**
     * @parameter
     */
    public KeystoreInfo keystoreInfo;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        //Two build projects: unsigned, signed (debug or developer)
        File signedOutputFile = new File( project.getBuild().getDirectory(), project.getBuild().getFinalName()
            + "-signed.apk" );

        File packagedResourceFile = new File( project.getBuild().getDirectory(), project.getBuild().getFinalName()
            + ".ap_" );
        File dexFile = new File( project.getBuild().getDirectory(), "classes.dex" );

        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        try
        {
            PrintStream printStream = ( verboseMode ) ? new PrintStream( bais ) : null;

            ApkBuilder signedBuilder = null;
            switch ( matchSigningType() )
            {
                case SIGN_DEBUG:

                    String keyStorePath = null;
                    String home = System.getProperty( "user.home" );
                    File f = new File( home, ".android/debug.keystore" );
                    if ( f.exists() )
                    {
                        keyStorePath = f.getAbsolutePath();
                    }
                    else
                    {
                        f = new File( home, "Local Settings\\Application Data\\Android\\debug.keystore" );
                        if ( !f.exists() )
                        {
                            throw new MojoExecutionException(
                                                              "Keystore not specificed and could not locate default debug.keystore" );
                        }
                        keyStorePath = f.getAbsolutePath();
                    }
                    getLog().info( "Signing with debug key: " + keyStorePath );
                    File signedDebugOutputFile = new File( project.getBuild().getDirectory(), project.getBuild()
                        .getFinalName() + "-signed.apk" );

                    signedBuilder = new ApkBuilder( signedDebugOutputFile.getAbsolutePath(),
                                                    packagedResourceFile.getAbsolutePath(), dexFile.getAbsolutePath(),
                                                    keyStorePath, printStream );
                    build( signedBuilder );
                    projectHelper.attachArtifact( project, "apk", "signed", signedDebugOutputFile );
                    break;
                case SIGN_AOSP:
                    getLog().info( "Signing with aosp key" );

                    InputStream pk = getClass().getClassLoader().getResourceAsStream( "security/shared.pk8" );
                    InputStream pem = getClass().getClassLoader().getResourceAsStream( "security/platform.x509.pem" );

                    SigningInfo signingInfo = new SigningInfo( getPrivateKeyFromStream( pk ), getCertFromStream( pem ) );
                    File signedAospOutputFile = new File( project.getBuild().getDirectory(), project.getBuild()
                        .getFinalName() + "-signed-aosp.apk" );

                    signedBuilder = createBuilder( signedAospOutputFile, packagedResourceFile, dexFile,
                                                   signingInfo.key, signingInfo.certificate, printStream );
                    build( signedBuilder );
                    projectHelper.attachArtifact( project, "apk", "signed", signedAospOutputFile );
                    break;
                case SIGN_PKCERT:
                    getLog().info( "Signing with private key and certificate" );
                    signingInfo = new SigningInfo( getPrivateKeyFromFile( privatekeyInfo.path ),
                                                   getCertFromFile( certificate.path ) );

                    signedBuilder = createBuilder( signedOutputFile, packagedResourceFile, dexFile, signingInfo.key,
                                                   signingInfo.certificate, printStream );
                    build( signedBuilder );
                    projectHelper.attachArtifact( project, "apk", "signed", signedOutputFile );
                    break;
                case SIGN_KEYSTORE:
                    getLog().info( "Signing with private key and keystore: keystore path = " + keystoreInfo.path );
                    signingInfo = loadKeyEntry( keystoreInfo.path, keystoreInfo.type,
                                                keystoreInfo.password.toCharArray(),
                                                privatekeyInfo.password.toCharArray(), privatekeyInfo.alias );
                    signedBuilder = createBuilder( signedOutputFile, packagedResourceFile, dexFile, signingInfo.key,
                                                   signingInfo.certificate, printStream );
                    build( signedBuilder );
                    projectHelper.attachArtifact( project, "apk", "signed", signedOutputFile );
                    break;

            }

            //Always do unsigned build
            File unsignedOutputFile = new File( project.getBuild().getDirectory(), project.getBuild().getFinalName()
                + "-unsigned.apk" );

            ApkBuilder unsignedBuilder = createBuilder( unsignedOutputFile, packagedResourceFile, dexFile, null,
                                                        printStream );
            build( unsignedBuilder );
            projectHelper.attachArtifact( project, "apk", "unsigned", unsignedOutputFile );

            if ( verboseMode )
            {
                //Just write verbose output all out at once 
                getLog().info( new String( bais.toByteArray() ) );
            }

        }
        catch ( ApkCreationException e )
        {
            e.printStackTrace();
            throw new MojoExecutionException( "ApkCreationException", e );
        }
        catch ( SealedApkException e )
        {
            e.printStackTrace();
            throw new MojoExecutionException( "SealedApkException", e );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new MojoExecutionException( "", e );
        }
    }

    private void build( ApkBuilder builder )
        throws DuplicateFileException, ApkCreationException, SealedApkException
    {
        if ( debugMode )
        {
            builder.setDebugMode( true );
        }
        if ( nativeLibraries != null )
        {
            //	builder.addNativeLibraries(nativeLibraries.path, nativeLibraries.abiFilter);			
        }

        builder.sealApk();
    }

    private static X509Certificate getCertFromFile( File file )
        throws Exception
    {
        InputStream inStream = new FileInputStream( file );
        return getCertFromStream( inStream );
    }

    private static X509Certificate getCertFromStream( InputStream inStream )
        throws Exception
    {
        X509Certificate cert = (X509Certificate) CertificateFactory.getInstance( "X.509" )
            .generateCertificate( inStream );
        inStream.close();
        return cert;
    }

    private static PrivateKey getPrivateKeyFromFile( File file )
        throws Exception
    {
        return getPrivateKeyFromStream( new FileInputStream( file ) );
    }

    private static PrivateKey getPrivateKeyFromStream( InputStream inStream )
        throws Exception
    {
        PKCS8EncodedKeySpec kspec = new PKCS8EncodedKeySpec( IOUtil.toByteArray( inStream ) );
        KeyFactory kf = KeyFactory.getInstance( "RSA" );
        return kf.generatePrivate( kspec );
    }

    private ApkBuilder createBuilder( File apkFile, File resFile, File dexFile, String storeOsPath,
                                      final PrintStream verboseStream )
        throws Exception
    {
        return new ApkBuilder( apkFile, resFile, dexFile, storeOsPath, verboseStream );
    }

    private ApkBuilder createBuilder( File apkFile, File resFile, File dexFile, PrivateKey key,
                                      X509Certificate certificate, PrintStream verboseStream )
        throws Exception
    {
        return new ApkBuilder( apkFile, resFile, dexFile, key, certificate, verboseStream );
    }

    private final static class SigningInfo
    {
        public final PrivateKey key;

        public final X509Certificate certificate;

        private SigningInfo( PrivateKey key, X509Certificate certificate )
        {
            if ( key == null && certificate == null )
            {
                throw new IllegalArgumentException( "key and certificate cannot both be null" );
            }
            this.key = key;
            this.certificate = certificate;
        }

        private SigningInfo( ApkBuilder.SigningInfo signingInfo )
        {
            this( signingInfo.key, signingInfo.certificate );
        }
    }

    private SigningInfo loadKeyEntry( String osKeyStorePath, String storeType, char[] keyStorePassword,
                                      char[] privateKeyPassword, String alias )
        throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
        UnrecoverableEntryException
    {
        try
        {
            KeyStore keyStore = KeyStore.getInstance( storeType != null ? storeType : KeyStore.getDefaultType() );
            FileInputStream fis = new FileInputStream( osKeyStorePath );
            keyStore.load( fis, keyStorePassword );
            fis.close();
            KeyStore.PrivateKeyEntry store = (KeyStore.PrivateKeyEntry) keyStore
                .getEntry( alias, new KeyStore.PasswordProtection( privateKeyPassword ) );
            return new SigningInfo( store.getPrivateKey(), (X509Certificate) store.getCertificate() );
        }
        catch ( FileNotFoundException e )
        {
            getLog().error( "Failed to load key: alias = " + alias + ", path = " + osKeyStorePath );
            e.printStackTrace();

        }

        return null;
    }

    // Matching signing strategy
    private static final int SIGN_KEYSTORE = 0x0;

    private static final int SIGN_PKCERT = 0x1;

    private static final int SIGN_DEBUG = 0x2;

    private static final int SIGN_AOSP = 0x3;

    private int matchSigningType()
        throws MojoExecutionException
    {
        if ( isAospSigned == true )
        {
            if ( keystoreInfo != null || certificate != null || privatekeyInfo != null )
            {
                throw new MojoExecutionException( "AospSigned but contains additional keystore or certificate params" );
            }
            return SIGN_AOSP;
        }
        else if ( certificate == null )
        {
            if ( keystoreInfo == null && privatekeyInfo == null )
            {
                return SIGN_DEBUG;
            }
            else if ( privatekeyInfo != null && keystoreInfo != null )
            {//TODO: check params
                return SIGN_KEYSTORE;
            }
            throw new MojoExecutionException( "Key strategy unknown, no enough params assigned" );
        }
        else
        {
            if ( keystoreInfo != null )
            {
                throw new MojoExecutionException( "Keystore param incompatible with certificate" );
            }
            return SIGN_PKCERT;
        }
    }

}
