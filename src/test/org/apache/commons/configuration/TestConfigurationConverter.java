package org.apache.commons.configuration;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.collections.ExtendedProperties;
import java.util.Vector;


/**
 * Tests the ConfigurationConverter class
 *
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @version $Id: TestConfigurationConverter.java,v 1.1 2003/12/23 15:09:05 epugh Exp $
 */
public class TestConfigurationConverter extends TestCase
{
    protected Configuration config = (Configuration) new BaseConfiguration();

    public TestConfigurationConverter(String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
        return new TestSuite( TestConfigurationConverter.class );
    }

    public static void main(String args[])
    {
        String[] testCaseName = { TestConfigurationConverter.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    public void testConverter()
    {
        config.setProperty("string", "teststring");
        config.setProperty("int", "123");
        Vector vec = new Vector();
        vec.add("item 1");
        vec.add("item 2");
        config.setProperty("vector", vec);

        ExtendedProperties ep = ConfigurationConverter
                .getExtendedProperties(config);


        assertEquals("This returns 'teststring'", ep.getString("string"),
                "teststring");
        Vector v = ep.getVector("vector");
        assertEquals("This returns 'item 1'", (String) v.get(0), "item 1");
        assertEquals("This returns 123", ep.getInt("int"), 123);

        Configuration c = ConfigurationConverter.getConfiguration(ep);


        assertEquals("This returns 'teststring'", c.getString("string"),
                "teststring");
        Vector v1 = c.getVector("vector");
        assertEquals("This returns 'item 1'", (String) v1.get(0), "item 1");
        assertEquals("This returns 123", c.getInt("int"), 123);
    }
}
