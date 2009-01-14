/*
 This file is part of the OpenNMS(R) Application.

 OpenNMS(R) is Copyright (C) 2002-2006 The OpenNMS Group, Inc.  All rights reserved.
 OpenNMS(R) is a derivative work, containing both original code, included code and modified
 code that was published under the GNU General Public License. Copyrights for modified 
 and included code are below.

 OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

 Modifications:

 2007 Aug 25: Implement SpringServiceDaemon. - dj@opennms.org
 2007 Jun 24: Use Java 5 generics. - dj@opennms.org
 2006 May 11: Added Event parameter support for setting the URL and foreignSource
 2004 Dec 27: Changed SQL_RETRIEVE_INTERFACES to omit interfaces that have been
              marked as deleted.
 2004 Feb 12: Rebuild the package to ip list mapping while a new discoveried interface
              to be scheduled.
 2003 Jan 31: Cleaned up some unused imports.
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.                                                            

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
       
 For more information contact: 
      OpenNMS Licensing       <license@opennms.org>
      http://www.opennms.org/
      http://www.opennms.com/

*/

package org.opennms.netmgt.provision.service;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.EventSubscriptionService;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

@EventListener(name="Provisiond:EventListener")
public class Provisioner extends BaseProvisioner implements SpringServiceDaemon {
	
	public static final String NAME = "Provisioner";

	private volatile Resource m_importResource;
	private volatile EventSubscriptionService m_eventSubscriptionService;
	private volatile EventForwarder m_eventForwarder;
	private volatile TimeTrackingMonitor m_stats;

            
	public void doImport() {
	    doImport(null);
	}
        
    /**
     * Begins importing from resource specified in model-importer.properties file or
     * in event parameter: url.  Import Resources are managed with a "key" called 
     * "foreignSource" specified in the XML retreived by the resource and can be overridden 
     * as a parameter of an event.
     * @param event
     */
	@EventHandler(uei=EventConstants.RELOAD_IMPORT_UEI)
    public void doImport(Event event) {
        Resource resource = null;
        try {
            m_stats = new TimeTrackingMonitor();
            
            resource = ((event != null && getEventUrl(event) != null) ? new UrlResource(getEventUrl(event)) : m_importResource); 
            String foreignSource = event == null ? null : getEventForeignSource(event); 

            send(importStartedEvent(resource));
            

			importModelFromResource(foreignSource, resource, m_stats);

			log().info("Finished Importing: "+m_stats);

			send(importSuccessEvent(m_stats, resource));

        } catch (Exception e) {
            String msg = "Exception importing "+resource;
			log().error(msg, e);
            send(importFailedEvent((msg+": "+e.getMessage()), resource));
        }
    }
    
    /**
     * @param e
     */
    @EventHandler(uei=EventConstants.NODE_ADDED_EVENT_UEI)
    public void handleNodeAddedEvent(Event e) {
        NodeScanSchedule scheduleForNode = getProvisionService().getScheduleForNode(new Long(e.getNodeid()).intValue());
        if (scheduleForNode != null) {
            addToScheduleQueue(scheduleForNode);
        }
    }
    

    /**
     * @param e
     */
    @EventHandler(uei=EventConstants.NODE_DELETED_EVENT_UEI)
    public void handleNodeDeletedEvent(Event e) {
        removeNodeFromScheduleQueue(new Long(e.getNodeid()).intValue());
    }
    
    private String getEventUrl(Event event) {
        return EventUtils.getParm(event, EventConstants.PARM_URL);
    }
    
    public String getStats() { return (m_stats == null ? "No Stats Availabile" : m_stats.toString()); }

    private Event importSuccessEvent(TimeTrackingMonitor stats, Resource resource) {

        return new EventBuilder( EventConstants.IMPORT_SUCCESSFUL_UEI, NAME )
            .addParam( EventConstants.PARM_IMPORT_RESOURCE, resource.toString() )
            .addParam( EventConstants.PARM_IMPORT_STATS, stats.toString() )
            .getEvent();
    }
    
	private void send(Event event) {
        m_eventForwarder.sendNow(event);
    }

    private Event importFailedEvent(String msg, Resource resource) {

        return new EventBuilder( EventConstants.IMPORT_FAILED_UEI, NAME )
            .addParam( EventConstants.PARM_IMPORT_RESOURCE, resource.toString() )
            .addParam( EventConstants.PARM_FAILURE_MESSAGE, msg )
            .getEvent();
    }

	private Event importStartedEvent(Resource resource) {

	    return new EventBuilder( EventConstants.IMPORT_STARTED_UEI, NAME )
            .addParam( EventConstants.PARM_IMPORT_RESOURCE, resource.toString() )
            .getEvent();
    }

    public void setImportResource(Resource resource) {
        m_importResource = resource;
    }

	public EventSubscriptionService getEventSubscriptionService() {
	    return m_eventSubscriptionService;
	}

	public void setEventSubscriptionService(EventSubscriptionService eventManager) {
		m_eventSubscriptionService = eventManager;
	}

	public void setEventForwarder(EventForwarder eventForwarder) {
	    m_eventForwarder = eventForwarder;
	}
	
	public void afterPropertiesSet() throws Exception {
	    super.afterPropertiesSet();
	}

    protected String getEventForeignSource(Event event) {
        return EventUtils.getParm(event, EventConstants.PARM_FOREIGN_SOURCE);
    }

    public void start() throws Exception {
        scheduleRescanForExistingNodes();
    }

}
