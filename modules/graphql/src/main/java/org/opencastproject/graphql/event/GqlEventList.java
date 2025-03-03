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

package org.opencastproject.graphql.event;


import org.opencastproject.elasticsearch.api.SearchResult;
import org.opencastproject.elasticsearch.api.SearchResultItem;
import org.opencastproject.elasticsearch.index.objects.event.Event;
import org.opencastproject.graphql.type.output.OffsetPageInfo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;

@GraphQLName(GqlEventList.TYPE_NAME)
@GraphQLNonNull
@GraphQLDescription("A list of events")
public class GqlEventList {

  public static final String TYPE_NAME = "EventList";

  protected final SearchResult<Event> searchResult;

  public GqlEventList(SearchResult<Event> searchResult) {
    this.searchResult = searchResult;
  }

  @GraphQLField
  @GraphQLNonNull
  public Long totalCount() {
    return searchResult.getHitCount();
  }

  @GraphQLField
  @GraphQLNonNull
  public OffsetPageInfo pageInfo() {
    return OffsetPageInfo.from(searchResult);
  }

  @GraphQLField
  @GraphQLNonNull
  public List<GqlEvent> nodes() {
    return Arrays.stream(searchResult.getItems())
        .map(SearchResultItem::getSource)
        .map(GqlEvent::new)
        .collect(Collectors.toList());
  }

}
