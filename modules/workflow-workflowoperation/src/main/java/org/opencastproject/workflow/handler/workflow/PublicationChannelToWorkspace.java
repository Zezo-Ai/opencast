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
package org.opencastproject.workflow.handler.workflow;

import org.opencastproject.job.api.JobContext;
import org.opencastproject.mediapackage.Attachment;
import org.opencastproject.mediapackage.Catalog;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.mediapackage.Publication;
import org.opencastproject.mediapackage.Track;
import org.opencastproject.mediapackage.selector.AttachmentSelector;
import org.opencastproject.mediapackage.selector.CatalogSelector;
import org.opencastproject.mediapackage.selector.TrackSelector;
import org.opencastproject.workflow.api.AbstractWorkflowOperationHandler;
import org.opencastproject.workflow.api.ConfiguredTagsAndFlavors;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowOperationException;
import org.opencastproject.workflow.api.WorkflowOperationHandler;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.api.WorkflowOperationResult;
import org.opencastproject.workflow.api.WorkflowOperationResult.Action;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Workflow operation handler to move elements from publication channel to workspace
 */
@Component(
    immediate = true,
    service = WorkflowOperationHandler.class,
    property = {
        "service.description=move publication elements to workspace",
        "workflow.operation=publication-channel-to-workspace"
    }
)

public class PublicationChannelToWorkspace extends AbstractWorkflowOperationHandler {

  /** Configuration key for the "source-channel" to use as a source input */
  static final String OPT_SOURCE_PUBLICATION_CHANNEL = "source-channel";

  /** The logging facility */
  private static final Logger logger = LoggerFactory
          .getLogger(PublicationChannelToWorkspace.class);

  @Override
  public WorkflowOperationResult start(WorkflowInstance workflowInstance, JobContext context)
      throws WorkflowOperationException {

    logger.info("Copying artifacts from published media package {} to workspace", workflowInstance.getId());
    final MediaPackage mediaPackage = workflowInstance.getMediaPackage();

    WorkflowOperationInstance currentOperation = workflowInstance.getCurrentOperation();

    ConfiguredTagsAndFlavors tagsAndFlavors = getTagsAndFlavors(workflowInstance, Configuration.many,
        Configuration.many, Configuration.many, Configuration.many);
    List<MediaPackageElementFlavor> configuredSourceFlavors = tagsAndFlavors.getSrcFlavors();
    List<String> configuredSourceTags = tagsAndFlavors.getSrcTags();
    List <String> configuredTargetTags = tagsAndFlavors.getTargetTags();
    String publicationChannel = StringUtils
        .trimToEmpty(currentOperation.getConfiguration(OPT_SOURCE_PUBLICATION_CHANNEL));
    if (publicationChannel.isEmpty()) {
      logger.error("No source publication-channel set.");
      throw new WorkflowOperationException("No source Publication channel set.");
    }

    Optional<Publication> publication = Arrays.stream(mediaPackage.getPublications())
        .filter(channel -> channel.getChannel().equals(publicationChannel)).findFirst();

    // Skip if the media package does not contain a matching publication
    if (publication.isEmpty()) {
      logger.warn("The media package '{}' contains no publications on channel '{}', skipping.",
          mediaPackage.getIdentifier(), publicationChannel);
      return createResult(mediaPackage, Action.SKIP);
    }

    //get tracks from publicationchannel with tags and flavors
    TrackSelector trackSelector = new TrackSelector();
    for (MediaPackageElementFlavor flavor : configuredSourceFlavors) {
      trackSelector.addFlavor(flavor);
    }
    for (String tag : configuredSourceTags) {
      trackSelector.addTag(tag);
    }
    Collection<Track> tracks = trackSelector.select(mediaPackage, publicationChannel, false);

    if (!configuredTargetTags.isEmpty()) {
      tracks.forEach(track -> {
        configuredTargetTags.forEach(targetTag -> track.addTag(targetTag.toString()));
      });
    }
    tracks.forEach(mediaPackage::add);

    //get attachements from publicationchannel with tags and flavors
    AttachmentSelector attachmentSelector = new AttachmentSelector();
    for (MediaPackageElementFlavor flavor : configuredSourceFlavors) {
      attachmentSelector.addFlavor(flavor);
    }
    for (String tag : configuredSourceTags) {
      attachmentSelector.addTag(tag);
    }
    Collection<Attachment> attachments = attachmentSelector.select(mediaPackage, publicationChannel, false);

    if (!configuredTargetTags.isEmpty()) {
      attachments.forEach(attachment -> {
        configuredTargetTags.forEach(targetTag -> attachment.addTag(targetTag.toString()));
      });
    }
    attachments.forEach(mediaPackage::add);

    //get catalogs from publicationchannel with tags and flavors
    CatalogSelector catalogSelector = new CatalogSelector();
    for (MediaPackageElementFlavor flavor : configuredSourceFlavors) {
      catalogSelector.addFlavor(flavor);
    }
    for (String tag : configuredSourceTags) {
      catalogSelector.addTag(tag);
    }
    Collection<Catalog> catalogs = catalogSelector.select(mediaPackage, publicationChannel, false);

    if (!configuredTargetTags.isEmpty()) {
      catalogs.forEach(catalog -> {
        configuredTargetTags.forEach(targetTag -> catalog.addTag(targetTag.toString()));
      });
    }
    catalogs.forEach(mediaPackage::add);

    return createResult(mediaPackage, Action.CONTINUE, 0);
  }

}
