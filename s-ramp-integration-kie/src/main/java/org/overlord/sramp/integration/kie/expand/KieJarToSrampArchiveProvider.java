/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.sramp.integration.kie.expand;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchiveException;
import org.overlord.sramp.atom.archive.expand.registry.TypeHintInfo;
import org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveProvider;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.integration.kie.model.KieJarModel;

/**
 * Provides a JAR version of the {@link ZipToSrampArchive}.
 *
 */
public class KieJarToSrampArchiveProvider implements ZipToSrampArchiveProvider {

    private static final Set<String> acceptedTypes = new HashSet<String>();
	private static final Map<String, String> hintsMap;
    static {
        acceptedTypes.add(KieJarModel.TYPE_ARCHIVE); 
        Map<String, String>aMap = new TreeMap<String,String>();
        aMap.put("META-INF/kmodule.xml", KieJarModel.TYPE_ARCHIVE); //$NON-NLS-1$
        hintsMap = Collections.unmodifiableMap(aMap);
    }

    /**
     * Constructor.
     */
    public KieJarToSrampArchiveProvider() {
    }

    /**
     * @see org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveProvider#accept(org.overlord.sramp.common.ArtifactType)
     */
    @Override
    public boolean accept(ArtifactType artifactType) {
        if (artifactType.isExtendedType()) {
            String extendedType = artifactType.getExtendedType();
            return acceptedTypes.contains(extendedType);
        }
        return false;
    }

    /**
     * @see org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveProvider#createExtractor(org.overlord.sramp.common.ArtifactType, java.io.File)
     */
    @Override
    public ZipToSrampArchive createExtractor(ArtifactType artifactType, File zipFile) throws ZipToSrampArchiveException {
        return new KieJarToSrampArchive(zipFile);
    }

    /**
     * @see org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveProvider#createExtractor(org.overlord.sramp.common.ArtifactType, java.io.InputStream)
     */
    @Override
    public ZipToSrampArchive createExtractor(ArtifactType artifactType, InputStream zipStream) throws ZipToSrampArchiveException {
        return new KieJarToSrampArchive(zipStream);
    }
    
    @Override
	public TypeHintInfo getArchiveTypeHints() {
		return new TypeHintInfo(10,hintsMap);
	}

}
