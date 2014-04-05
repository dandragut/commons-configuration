/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * A test class for {@code InMemoryNodeModel} which tests functionality related
 * to node references. This test class creates a model for the test structure
 * with authors data. Each node is associated a string reference object with the
 * node name. It can then be checked whether updates of the hierarchy do not
 * affect the references.
 *
 * @version $Id$
 */
public class TestInMemoryNodeModelReferences
{
    /** A mock resolver. */
    private NodeKeyResolver<ImmutableNode> resolver;

    /** The test model. */
    private InMemoryNodeModel model;

    @Before
    public void setUp() throws Exception
    {
        resolver = NodeStructureHelper.createResolverMock();
        NodeStructureHelper.expectResolveKeyForQueries(resolver);
        NodeStructureHelper.expectResolveAddKeys(resolver);
        EasyMock.replay(resolver);
        model = new InMemoryNodeModel();
        Map<ImmutableNode, String> references = createReferences();
        model.mergeRoot(NodeStructureHelper.ROOT_AUTHORS_TREE, null,
                references, NodeStructureHelper.ROOT_AUTHORS_TREE.getNodeName(), resolver);
    }

    /**
     * Creates the initial reference data for the test hierarchy.
     *
     * @return the map with reference data
     */
    private Map<ImmutableNode, String> createReferences()
    {
        Collection<ImmutableNode> nodes =
                collectNodes(NodeStructureHelper.ROOT_AUTHORS_TREE);
        nodes.remove(NodeStructureHelper.ROOT_AUTHORS_TREE);
        Map<ImmutableNode, String> refs = new HashMap<ImmutableNode, String>();
        for (ImmutableNode node : nodes)
        {
            refs.put(node, node.getNodeName());
        }
        return refs;
    }

    /**
     * Returns a flat collection of all nodes contained in the specified nodes
     * hierarchy.
     *
     * @param root the root node of the hierarchy
     * @return a collection with all nodes in this hierarchy
     */
    private Collection<ImmutableNode> collectNodes(ImmutableNode root)
    {
        final Set<ImmutableNode> nodes = new HashSet<ImmutableNode>();
        NodeTreeWalker.INSTANCE.walkBFS(root,
                new ConfigurationNodeVisitorAdapter<ImmutableNode>()
                {
                    @Override
                    public void visitBeforeChildren(ImmutableNode node,
                            NodeHandler<ImmutableNode> handler)
                    {
                        nodes.add(node);
                    }
                }, model.getNodeHandler());
        return nodes;
    }

    /**
     * Tests whether the stored references can be queried.
     */
    @Test
    public void testQueryReferences()
    {
        ReferenceNodeHandler handler = model.getReferenceNodeHandler();
        Collection<ImmutableNode> nodes = collectNodes(handler.getRootNode());
        for (ImmutableNode node : nodes)
        {
            assertEquals("Wrong reference", node.getNodeName(),
                    handler.getReference(node));
        }
    }

    /**
     * Tests the reference returned for an unknown node.
     */
    @Test
    public void testQueryReferenceUnknown()
    {
        ReferenceNodeHandler handler = model.getReferenceNodeHandler();
        assertNull("Got a reference",
                handler.getReference(new ImmutableNode.Builder().create()));
    }

    /**
     * Helper method for checking addReferences() if no action should be
     * performed.
     *
     * @param refs the map with references
     */
    private void checkAddReferencesNoOp(Map<ImmutableNode, ?> refs)
    {
        ReferenceNodeHandler handler = model.getReferenceNodeHandler();
        model.addReferences(refs);
        assertSame("Model was changed", handler,
                model.getReferenceNodeHandler());
    }

    /**
     * Tests whether addReferences() can handle null input.
     */
    @Test
    public void testAddReferencesNull()
    {
        checkAddReferencesNoOp(null);
    }

    /**
     * Tests addReferences() for an empty map.
     */
    @Test
    public void testAddReferencesEmpty()
    {
        checkAddReferencesNoOp(new HashMap<ImmutableNode, Object>());
    }

    /**
     * Tests whether references can be queried after an update operation.
     */
    @Test
    public void testQueryReferencesAfterUpdate()
    {
        model.addProperty("Simmons.Hyperion", Collections.singleton("Lamia"),
                resolver);
        ReferenceNodeHandler handler = model.getReferenceNodeHandler();
        assertEquals("Wrong reference 1", "Hyperion",
                handler.getReference(NodeStructureHelper.nodeForKey(model,
                        "Simmons/Hyperion")));
        assertEquals("Wrong reference 2", "Simmons",
                handler.getReference(NodeStructureHelper.nodeForKey(model,
                        "Simmons")));
    }

    /**
     * Tests whether the removed references can be queried if there are none.
     */
    @Test
    public void testQueryRemovedReferencesEmpty()
    {
        ReferenceNodeHandler handler = model.getReferenceNodeHandler();
        assertTrue("Got removed references", handler.removedReferences()
                .isEmpty());
    }

    /**
     * Tests whether removed references can be queried.
     */
    @Test
    public void testQueryRemovedReferencesAfterRemove()
    {
        model.clearTree("Simmons", resolver);
        ReferenceNodeHandler handler = model.getReferenceNodeHandler();
        List<Object> removedRefs = handler.removedReferences();
        int authorIdx = 2;
        for (int i = 0; i < NodeStructureHelper.worksLength(authorIdx); i++)
        {
            assertTrue(
                    "Work not found: " + i,
                    removedRefs.contains(NodeStructureHelper.work(authorIdx, i)));
            for (int j = 0; j < NodeStructureHelper
                    .personaeLength(authorIdx, i); j++)
            {
                assertTrue("Persona not found: " + j,
                        removedRefs.contains(NodeStructureHelper.persona(
                                authorIdx, i, j)));
            }
        }
    }

    /**
     * Tests that the list with removed references cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemovedReferencesModify()
    {
        model.clearTree("Simmons", resolver);
        ReferenceNodeHandler handler = model.getReferenceNodeHandler();
        List<Object> removedRefs = handler.removedReferences();
        removedRefs.add("another one");
    }

    /**
     * Tests whether a value is taken into account when the root node is merged.
     */
    @Test
    public void testMergeRootWithValue()
    {
        ImmutableNode node = NodeStructureHelper.createNode("newNode", "test");
        model.mergeRoot(node, null, null, null, resolver);
        ImmutableNode root = model.getNodeHandler().getRootNode();
        assertEquals("Wrong node name",
                NodeStructureHelper.ROOT_AUTHORS_TREE.getNodeName(),
                root.getNodeName());
        assertEquals("Wrong node value", "test", root.getValue());
    }

    /**
     * Tests whether the name of the root node can be changed during a merge
     * operation.
     */
    @Test
    public void testMergeRootOverrideName()
    {
        ImmutableNode node = NodeStructureHelper.createNode("newNode", null);
        final String newName = "newRootNode";

        model.mergeRoot(node, newName, null, null, resolver);
        ImmutableNode root = model.getNodeHandler().getRootNode();
        assertEquals("Wrong root name", newName, root.getNodeName());
    }

    /**
     * Tests whether attributes are taken into account by a merge operation.
     */
    @Test
    public void testMergeRootWithAttributes()
    {
        ImmutableNode node =
                new ImmutableNode.Builder().addAttribute("key", "value")
                        .create();
        model.mergeRoot(node, null, null, null, resolver);
        ImmutableNode root = model.getNodeHandler().getRootNode();
        assertEquals("Wrong number of attributes", 1, root.getAttributes()
                .size());
        assertEquals("Wrong attribute", "value", root.getAttributes()
                .get("key"));
    }

    /**
     * Tests whether mergeRoot() handles an explicit reference object for the
     * root node correctly.
     */
    @Test
    public void testMergeRootReference()
    {
        final Object rootRef = 20140404210508L;
        ImmutableNode node = NodeStructureHelper.createNode("newNode", null);

        model.mergeRoot(node, null, null, rootRef, resolver);
        ReferenceNodeHandler refHandler = model.getReferenceNodeHandler();
        ImmutableNode checkNode =
                NodeStructureHelper.nodeForKey(model, "Simmons/Ilium");
        assertEquals("Wrong reference for node", checkNode.getNodeName(),
                refHandler.getReference(checkNode));
        assertEquals("Wrong root reference", rootRef,
                refHandler.getReference(refHandler.getRootNode()));
    }
}