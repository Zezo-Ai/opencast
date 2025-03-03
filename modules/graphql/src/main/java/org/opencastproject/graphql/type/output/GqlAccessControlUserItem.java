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

package org.opencastproject.graphql.type.output;

import org.opencastproject.graphql.execution.context.OpencastContextManager;
import org.opencastproject.security.api.AccessControlEntry;
import org.opencastproject.security.api.UserDirectoryService;
import org.opencastproject.userdirectory.UserIdRoleProvider;

import java.util.Set;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

@GraphQLName(GqlAccessControlUserItem.TYPE_NAME)
public class GqlAccessControlUserItem extends AbstractAccessControlItem implements GqlAccessControlItem {

  public static final String TYPE_NAME = "AccessControlUserItem";

  public GqlAccessControlUserItem(AccessControlEntry accessControlEntry) {
    super(accessControlEntry);
  }

  public GqlAccessControlUserItem(Set<AccessControlEntry> accessControlEntries) {
    super(accessControlEntries);
  }

  @GraphQLField
  public String label() {
    UserDirectoryService uds = OpencastContextManager.getCurrentContext().getService(UserDirectoryService.class);
    String username = getUniqueRole().substring(UserIdRoleProvider.getUserIdRole("").length()).toLowerCase();
    var user = uds.loadUser(username);
    String userEmail = (user != null && user.getEmail() != null && !user.getEmail().isEmpty())
        ? " (" + user.getEmail() + ")"
        : "";
    return (user != null ? user.getName() : username) + userEmail;
  }

}
