/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collection.api;


/**
 * <p>CollectionAttributeType interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CollectionAttributeType {

    /**
     * <p>The type of metric that the attribute represents. Valid case-insensitive values are:</p>
     * <ul>
     * <li>counter</li>
     * <li>gauge</li>
     * <li>string</li>
     * </ul>
     *
     * @return a {@link java.lang.String} object.
     */
    String getType();

    /**
     * Human readable name for the attribute. Normally these are specified by an "alias" field
     * in the data collection configuration.
     * 
     * @return a {@link java.lang.String} object.
     */
    String getName();
    
    /**
     * <p>equals</p>
     *
     * @param o a {@link java.lang.Object} object.
     * @return a boolean.
     */
    @Override
    boolean equals(Object o);
    
    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    @Override
    int hashCode();
    
    /**
     * <p>getGroupType</p>
     *
     * @return a {@link org.opennms.netmgt.collection.api.AttributeGroupType} object.
     */
    AttributeGroupType getGroupType();
    
    /**
     * <p>storeAttribute</p>
     *
     * @param attribute a {@link org.opennms.netmgt.collection.api.CollectionAttribute} object.
     * @param persister a {@link org.opennms.netmgt.collection.api.Persister} object.
     */
    void storeAttribute(CollectionAttribute attribute, Persister persister);
}
