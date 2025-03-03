/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 *
 * The Apereo Foundation licenses this file to you under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at:
 *
 *   http://opensource.org/licenses/ecl2.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package org.opencastproject.index.service.impl.util;

import org.opencastproject.elasticsearch.index.objects.event.Event;
import org.opencastproject.list.api.ResourceListQuery;
import org.opencastproject.mediapackage.Publication;
import org.opencastproject.metadata.dublincore.DublinCore;
import org.opencastproject.metadata.dublincore.DublinCoreMetadataCollection;
import org.opencastproject.metadata.dublincore.EventCatalogUIAdapter;
import org.opencastproject.metadata.dublincore.MetadataField;
import org.opencastproject.util.DateTimeSupport;
import org.opencastproject.workflow.handler.distribution.EngagePublicationChannel;
import org.opencastproject.workflow.handler.distribution.InternalPublicationChannel;

import com.entwinemedia.fn.Fn;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public final class EventUtils {

  public static final Map<String, String> PUBLICATION_CHANNELS = new HashMap<>();
  public static final String ENGAGE_LIVE_CHANNEL_ID  = "engage-live";

  static {
    PUBLICATION_CHANNELS.put(EngagePublicationChannel.CHANNEL_ID, "EVENTS.EVENTS.DETAILS.PUBLICATIONS.ENGAGE");
    PUBLICATION_CHANNELS.put("youtube", "EVENTS.EVENTS.DETAILS.PUBLICATIONS.YOUTUBE");
    PUBLICATION_CHANNELS.put(ENGAGE_LIVE_CHANNEL_ID, "EVENTS.EVENTS.DETAILS.PUBLICATIONS.ENGAGE_LIVE");
  }

  private EventUtils() {
  }

  /**
   * Loads the metadata for the given event
   *
   * @param event
   *          the source {@link Event}
   * @param eventCatalogUIAdapter
   *          the catalog definition
   * @return a {@link DublinCoreMetadataCollection} instance with all the event metadata
   *
   * @throws ParseException
   */
  public static DublinCoreMetadataCollection getEventMetadata(Event event, EventCatalogUIAdapter eventCatalogUIAdapter)
          throws ParseException {
    DublinCoreMetadataCollection eventMetadata = new DublinCoreMetadataCollection(eventCatalogUIAdapter.getRawFields());
    setEventMetadataValues(event, eventMetadata);
    return eventMetadata;
  }

  /**
   * Loads the metadata for the given event
   *
   * @param event
   *          the source {@link Event}
   * @param eventCatalogUIAdapter
   *          the catalog definition
   * @param collectionQueryOverride
   *          a custom list provider query mapped to every metadata field.
   *
   * @return a {@link DublinCoreMetadataCollection} instance with all the event metadata
   *
   * @throws ParseException
   */
  public static DublinCoreMetadataCollection getEventMetadata(Event event, EventCatalogUIAdapter eventCatalogUIAdapter,
      ResourceListQuery collectionQueryOverride) throws ParseException {
    DublinCoreMetadataCollection eventMetadata = new DublinCoreMetadataCollection(
        eventCatalogUIAdapter.getRawFields(collectionQueryOverride));
    setEventMetadataValues(event, eventMetadata);
    return eventMetadata;
  }

  /**
   * Set values of metadata fields from event.
   *
   * @param event
   *          the {@link Event} from the index
   * @param eventMetadata
   *          a {@link DublinCoreMetadataCollection} to be modified
   *
   * @throws ParseException
   */
  public static void setEventMetadataValues(Event event, DublinCoreMetadataCollection eventMetadata)
          throws ParseException {
    for (MetadataField field: eventMetadata.getOutputFields().values()) {
      if (field.getOutputID().equals(DublinCore.PROPERTY_TITLE.getLocalName())) {
        field.setValue(event.getTitle());
      }
      else if (field.getOutputID().equals(DublinCore.PROPERTY_SUBJECT.getLocalName())) {
        field.setValue(event.getSubject());
      }
      else if (field.getOutputID().equals(DublinCore.PROPERTY_DESCRIPTION.getLocalName())) {
        field.setValue(event.getDescription());
      }
      else if (field.getOutputID().equals(DublinCore.PROPERTY_LANGUAGE.getLocalName())) {
        field.setValue(event.getLanguage());
      }
      else if (field.getOutputID().equals(DublinCore.PROPERTY_RIGHTS_HOLDER.getLocalName())) {
        field.setValue(event.getRights());
      }
      else if (field.getOutputID().equals(DublinCore.PROPERTY_LICENSE.getLocalName())) {
        field.setValue(event.getLicense());
      }
      else if (field.getOutputID().equals(DublinCore.PROPERTY_IS_PART_OF.getLocalName())) {
        field.setValue(event.getSeriesId());
      }
      else if (field.getOutputID().equals(DublinCore.PROPERTY_CREATOR.getLocalName())) {
        field.setValue(event.getPresenters());
      }
      else if (field.getOutputID().equals(DublinCore.PROPERTY_CONTRIBUTOR.getLocalName())) {
        field.setValue(event.getContributors());
      }
      else if (field.getOutputID().equals(DublinCore.PROPERTY_SOURCE.getLocalName())) {
        field.setValue(event.getSource());
      }
      else if (field.getOutputID().equals(DublinCore.PROPERTY_CREATED.getLocalName())) {

        String createdDate = event.getCreated();
        if (StringUtils.isNotBlank(createdDate)) {
          field.setValue(new Date(DateTimeSupport.fromUTC(createdDate)));
        }
      }
      else if (field.getOutputID().equals(DublinCore.PROPERTY_IDENTIFIER.getLocalName())) {
        field.setValue(event.getIdentifier());
      }
      else if (field.getOutputID().equals(DublinCore.PROPERTY_PUBLISHER.getLocalName())) {
        field.setValue(event.getPublisher());
      }
      else if (field.getOutputID().equals("duration")) {
        Long duration = event.getDuration();
        if (duration != null) {
          field.setValue(event.getDuration().toString());
        }
      }
      else if (field.getOutputID().equals("location")) {
        field.setValue(event.getLocation());
      }
      else if (field.getOutputID().equals("startDate")) {
        String recordingStartDate = event.getRecordingStartDate();
        if (StringUtils.isNotBlank(recordingStartDate)) {
          Date startDateTime = new Date(DateTimeSupport.fromUTC(recordingStartDate));
          SimpleDateFormat sdf = new SimpleDateFormat(field.getPattern());
          sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
          field.setValue(sdf.format(startDateTime));
        }
      }
    }
  }

  /**
   * A filter to remove all internal channel publications.
   */
  public static final Fn<Publication, Boolean> internalChannelFilter = new Fn<Publication, Boolean>() {
    @Override
    public Boolean apply(Publication a) {
      if (InternalPublicationChannel.CHANNEL_ID.equals(a.getChannel()))
        return false;
      return true;
    }
  };
}
