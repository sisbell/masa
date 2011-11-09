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

import com.android.sdklib.build.ApkBuilder;
import com.android.sdklib.build.ApkCreationException;
import com.android.sdklib.build.SealedApkException;

import java.security.cert.CertificateException;
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
    private String keyStorePath;
     
    /*
     * @parameter
     */
    private String keyStoreType;
    
    /*
     * @parameter
     */
    private String keyStorePassword;    
    
    /*
     * @parameter
     */
    private String keyStoreAlias;       
    
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        File outputFile =
            new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + "-unsigned.apk" );

        File packagedResourceFile = new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + ".ap_" );
        File dexFile = new File( project.getBuild().getDirectory(), "classes.dex" );
        
        ByteArrayOutputStream bais = new ByteArrayOutputStream();   
		try {
			ApkBuilder unsignedBuilder = createBuilder(outputFile,
					packagedResourceFile, dexFile, null,
					(verboseMode) ? new PrintStream(bais) : null);
			
			//Will be either debug signed or dev signed
			PrintStream printStream = (verboseMode) ? new PrintStream(bais) : null;
			SigningInfo signingInfo = (!isDebugBuild()) ? loadKeyEntry(
					keyStorePath, keyStoreType, keyStorePassword.toCharArray(),
					keyStoreAlias) : new SigningInfo(ApkBuilder.getDebugKey(
					keyStorePath, printStream));

			ApkBuilder signedBuilder =  createBuilder(outputFile,
					packagedResourceFile, dexFile,signingInfo.key, signingInfo.certificate,
					printStream);
						;
				/*new ApkBuilder(outputFile.getAbsolutePath(),
					packagedResourceFile.getAbsolutePath(), dexFile.getAbsolutePath(), null,
					(verboseMode) ? new PrintStream(bais) : null);
					*/
			
			if(debugMode) {
				signedBuilder.setDebugMode(true);
			}	
			if(nativeLibraries != null) {
				signedBuilder.addNativeLibraries(nativeLibraries.path, nativeLibraries.abiFilter);
			}
			
			signedBuilder.sealApk();
			
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
  
        projectHelper.attachArtifact( project, "apk", "unsigned", outputFile );
    }
    
    private ApkBuilder createBuilder(File apkFile, File resFile, File dexFile, String debugStoreOsPath,
            final PrintStream verboseStream) {
    	return null;
    }
    
    private ApkBuilder createBuilder(File apkFile, File resFile, File dexFile, PrivateKey key,
            X509Certificate certificate, PrintStream verboseStream) {
    	return null;
    }
    
    private final static class SigningInfo {
        public final PrivateKey key;
        public final X509Certificate certificate;

        private SigningInfo(PrivateKey key, X509Certificate certificate) {
            if (key == null || certificate == null) {
                throw new IllegalArgumentException("key and certificate cannot be null");
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
		return this.keyStorePath != null;
	}
}
