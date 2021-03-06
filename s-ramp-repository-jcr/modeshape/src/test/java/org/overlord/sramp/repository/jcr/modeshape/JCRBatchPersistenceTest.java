/*
 * Copyright 2011 JBoss Inc
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
package org.overlord.sramp.repository.jcr.modeshape;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.repository.PersistenceManager.BatchItem;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.SrampQuery;


/**
 * Tests adding multiple artifacts at the same time via the persistence manager's batch
 * functionality.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRBatchPersistenceTest extends AbstractNoAuditingJCRPersistenceTest {

	@Test
	public void testSimpleBatch() throws Exception {
	    List<BatchItem> items = new ArrayList<BatchItem>();
	    BatchItem item = createBatchItem("/sample-files/batch/simple-1.txt", new Document(), BaseArtifactEnum.DOCUMENT); //$NON-NLS-1$
	    items.add(item);
	    item = createBatchItem("/sample-files/batch/simple-2.txt", new Document(), BaseArtifactEnum.DOCUMENT); //$NON-NLS-1$
        items.add(item);

        List<Object> response = persistenceManager.persistBatch(items);
        for (Object object : response) {
            Assert.assertTrue(object instanceof BaseArtifactType);
        }

        BaseArtifactType simple1 = (BaseArtifactType) response.get(0);
        BaseArtifactType simple2 = (BaseArtifactType) response.get(1);

		Assert.assertEquals("simple-1.txt", simple1.getName()); //$NON-NLS-1$
        Assert.assertEquals("simple-2.txt", simple2.getName()); //$NON-NLS-1$
	}

    @Test
    public void testWsdlBatch() throws Exception {
        List<BatchItem> items = new ArrayList<BatchItem>();
        BatchItem item = createBatchItem("/sample-files/wsdl/jcr-sample-externalrefs.xsd", new XsdDocument(), BaseArtifactEnum.XSD_DOCUMENT); //$NON-NLS-1$
        items.add(item);
        item = createBatchItem("/sample-files/wsdl/jcr-sample-externalrefs.wsdl", new WsdlDocument(), BaseArtifactEnum.WSDL_DOCUMENT); //$NON-NLS-1$
        items.add(item);

        List<Object> response = persistenceManager.persistBatch(items);
        for (Object object : response) {
            Assert.assertTrue(object instanceof BaseArtifactType);
        }

        XsdDocument xsd = (XsdDocument) response.get(0);
        Assert.assertNotNull(xsd);
        Assert.assertEquals("jcr-sample-externalrefs.xsd", xsd.getName()); //$NON-NLS-1$
        Assert.assertEquals("urn:s-ramp:test:jcr-sample-externalrefs:types", xsd.getTargetNamespace()); //$NON-NLS-1$

        WsdlDocument wsdl = (WsdlDocument) response.get(1);
        Assert.assertNotNull(wsdl);
        Assert.assertEquals("jcr-sample-externalrefs.wsdl", wsdl.getName()); //$NON-NLS-1$
        Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", wsdl.getTargetNamespace()); //$NON-NLS-1$

        ElementDeclaration extInput = (ElementDeclaration)
                assertSingleArtifact(ArtifactTypeEnum.ElementDeclaration, "extInput"); //$NON-NLS-1$
        ComplexTypeDeclaration extOutputType = (ComplexTypeDeclaration)
                assertSingleArtifact(ArtifactTypeEnum.ComplexTypeDeclaration, "extOutputType"); //$NON-NLS-1$
        SimpleTypeDeclaration extSimpleType = (SimpleTypeDeclaration)
                assertSingleArtifact(ArtifactTypeEnum.SimpleTypeDeclaration, "extSimpleType"); //$NON-NLS-1$
        Message findRequestMessage = (Message)
                assertSingleArtifact(ArtifactTypeEnum.Message, "findRequest"); //$NON-NLS-1$
        Message findResponseMessage = (Message)
                assertSingleArtifact(ArtifactTypeEnum.Message, "findResponse"); //$NON-NLS-1$
        Message findRequestSimpleMessage = (Message)
                assertSingleArtifact(ArtifactTypeEnum.Message, "findRequestSimple"); //$NON-NLS-1$

        // findRequestMessage assertions
        Part part = (Part) getArtifactByTarget(findRequestMessage.getPart().get(0));
        Assert.assertNull(part.getType());
        ElementDeclaration elem = (ElementDeclaration) getArtifactByTarget(part.getElement());
        Assert.assertEquals(extInput.getUuid(), elem.getUuid());
        // findResponseMessage assertions
        part = (Part) getArtifactByTarget(findResponseMessage.getPart().get(0));
        Assert.assertNull(part.getElement());
        ComplexTypeDeclaration complexType = (ComplexTypeDeclaration) getArtifactByTarget(part.getType());
        Assert.assertEquals(extOutputType.getUuid(), complexType.getUuid());
        // findRequestSimpleMessage assertions
        part = (Part) getArtifactByTarget(findRequestSimpleMessage.getPart().get(0));
        Assert.assertNull(part.getElement());
        SimpleTypeDeclaration type = (SimpleTypeDeclaration) getArtifactByTarget(part.getType());
        Assert.assertEquals(extSimpleType.getUuid(), type.getUuid());
    }

	/**
	 * Gets an artifact by a {@link Target}.
	 * @param target
	 * @throws Exception
	 */
	private BaseArtifactType getArtifactByTarget(Target target) throws Exception {
	    Assert.assertNotNull("Missing target/relationship.", target); //$NON-NLS-1$
		return getArtifactByUUID(target.getValue());
	}

	/**
	 * Ensures that a single artifact exists of the given type and name.
	 * @param type
	 * @param name
	 * @throws Exception
	 */
	private BaseArtifactType assertSingleArtifact(ArtifactTypeEnum type, String name) throws Exception {
		String q = String.format("/s-ramp/%1$s/%2$s[@name = ?]", type.getModel(), type.getType()); //$NON-NLS-1$
		SrampQuery query = queryManager.createQuery(q);
		query.setString(name);
		ArtifactSet artifactSet = null;
		try {
			artifactSet = query.executeQuery();
			Assert.assertEquals(1, artifactSet.size());
			BaseArtifactType arty = artifactSet.iterator().next();
			Assert.assertEquals(name, arty.getName());
			return arty;
		} finally {
			if (artifactSet != null)
				artifactSet.close();
		}
	}

	/**
	 * Gets a single artifact by UUID.
	 * @param uuid
	 * @throws Exception
	 */
	private BaseArtifactType getArtifactByUUID(String uuid) throws Exception {
		SrampQuery query = queryManager.createQuery("/s-ramp[@uuid = ?]"); //$NON-NLS-1$
		query.setString(uuid);
		ArtifactSet artifactSet = null;
		try {
			artifactSet = query.executeQuery();
			Assert.assertEquals(1, artifactSet.size());
			return artifactSet.iterator().next();
		} finally {
			if (artifactSet != null)
				artifactSet.close();
		}
	}

    /**
     * Creates a batch item for the given file.
     * @param filePath
     * @param document
     * @param type
     * @throws SrampException
     */
    private BatchItem createBatchItem(String filePath, DocumentArtifactType document, BaseArtifactEnum type) throws SrampException {
        String artifactFileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        InputStream contentStream = this.getClass().getResourceAsStream(filePath);

        document.setArtifactType(type);
        document.setName(artifactFileName);

        return new BatchItem(filePath, document, contentStream);
    }


}
