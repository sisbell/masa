/*
 * Copyright (C) 2007-2008 JVending Masa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jvending.masa.plugin.apkbuilder;

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

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;

import java.io.*;

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
    
    /*
     * Root folder containing native libraries to include in the application package.
     * 
     * @parameter
     */
    private NativeLibraries nativeLibraries;
    
    /*
     * @parameter
     */
    private boolean verboseMode;
    
    /*
     * @parameter
     */
    private KeyStoreObject keyStore;
    
    /*
     * @parameter
     */
    private X509Cert certificate;
    
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
    	//Two build projects: unsigned, signed (debug or developer)
        File unsignedOutputFile =
            new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + "-unsigned.apk" );

        File signedOutputFile =
            new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + "-signed.apk" );

        File signedDebugOutputFile =
            new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + "-signed-debug.apk" );
      
        File packagedResourceFile = new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + ".ap_" );
        File dexFile = new File( project.getBuild().getDirectory(), "classes.dex" );
        
        ByteArrayOutputStream bais = new ByteArrayOutputStream();   
		try {
			PrintStream printStream = (verboseMode) ? new PrintStream(bais) : null;
			
			ApkBuilder signedBuilder = null;
			if(isDebugBuild())
			{
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
	            }
				signedBuilder =	new ApkBuilder(signedDebugOutputFile.getAbsolutePath(),
						packagedResourceFile.getAbsolutePath(), dexFile.getAbsolutePath(), keyStorePath,
						printStream);	
				build(signedBuilder);
				projectHelper.attachArtifact( project, "apk", "signed-debug", signedDebugOutputFile );
			} 
			else
			{
				SigningInfo signingInfo = (keyStore != null) ? loadKeyEntry(
						keyStore.path, keyStore.type, keyStore.password.toCharArray(),
						keyStore.alias) : new SigningInfo(null,getCertFromFile(certificate.file) );		
				
				signedBuilder =  createBuilder(signedOutputFile,
						packagedResourceFile, dexFile, signingInfo.key, signingInfo.certificate,
						printStream);
				build(signedBuilder);
				projectHelper.attachArtifact( project, "apk", "signed", signedOutputFile );
			}
			
			
			ApkBuilder unsignedBuilder = createBuilder(unsignedOutputFile,
					packagedResourceFile, dexFile, null,
					printStream);
			build(unsignedBuilder);
			projectHelper.attachArtifact( project, "apk", "unsigned", unsignedOutputFile );
			/*
			ApkBuilder unsignedBuilder = createBuilder(outputFile,
					packagedResourceFile, dexFile, null,
					printStream);

			SigningInfo signingInfo = (!isDebugBuild()) ? loadKeyEntry(
					keyStorePath, keyStoreType, keyStorePassword.toCharArray(),
					keyStoreAlias) : new SigningInfo(ApkBuilder.getDebugKey(
					keyStorePath, printStream));
				
					*/

			
	        if(verboseMode)
	        {
	        	//Just write verbose output all out at once 
	        	getLog().info(new String(bais.toByteArray()));       	
	        }

		} catch (ApkCreationException e) {
			e.printStackTrace();
			throw new MojoExecutionException( "ApkCreationException", e );
		} catch (SealedApkException e) {
			e.printStackTrace();
			throw new MojoExecutionException( "SealedApkException", e );
		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoExecutionException( "", e );
		}     
    }
    
    private void build(ApkBuilder builder) throws DuplicateFileException, ApkCreationException, SealedApkException {
		if(debugMode) {
			builder.setDebugMode(true);
		}	
		if(nativeLibraries != null) {
			builder.addNativeLibraries(nativeLibraries.path, nativeLibraries.abiFilter);			
		}
		
		builder.sealApk();
    }
    
    private static X509Certificate getCertFromFile(File file) throws Exception {
    	 InputStream inStream = new FileInputStream(file);
    	 X509Certificate cert = (X509Certificate) CertificateFactory
    	 	.getInstance("X.509").generateCertificate(inStream);
    	 inStream.close();
    	 return cert;
    }
    
    private ApkBuilder createBuilder(File apkFile, File resFile, File dexFile, String debugStoreOsPath,
            final PrintStream verboseStream) throws Exception {
    	return new ApkBuilder(apkFile, resFile, dexFile, debugStoreOsPath, verboseStream);
    }
    
    private ApkBuilder createBuilder(File apkFile, File resFile, File dexFile, PrivateKey key,
            X509Certificate certificate, PrintStream verboseStream) throws Exception {
    	return new ApkBuilder(apkFile, resFile, dexFile, key, certificate, verboseStream);
    }
    
    private final static class SigningInfo {
        public final PrivateKey key;
        public final X509Certificate certificate;

        private SigningInfo(PrivateKey key, X509Certificate certificate) {
            if (key == null && certificate == null) {
                throw new IllegalArgumentException("key and certificate cannot both be null");
            }
            this.key = key;
            this.certificate = certificate;
        }
        
        private SigningInfo(ApkBuilder.SigningInfo signingInfo) {
        	this(signingInfo.key, signingInfo.certificate);
        }
    }
    
	private SigningInfo loadKeyEntry(String osKeyStorePath, String storeType, 
			char[] password, String alias )
			throws KeyStoreException, NoSuchAlgorithmException,
			CertificateException, IOException, UnrecoverableEntryException {
		try {
			KeyStore keyStore  = KeyStore
					.getInstance(storeType != null ? storeType : KeyStore
							.getDefaultType());
			FileInputStream fis = new FileInputStream(osKeyStorePath);
			keyStore.load(fis, password);
			fis.close();
			KeyStore.PrivateKeyEntry store = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias,
					new KeyStore.PasswordProtection(password));
			return new SigningInfo(store.getPrivateKey(), (X509Certificate) store.getCertificate());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	private boolean isDebugBuild() {
		return keyStore == null && certificate == null;
	}
}
