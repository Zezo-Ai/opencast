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

package org.opencastproject.adopter.registration;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.opencastproject.util.RestUtil.R.ok;
import static org.opencastproject.util.RestUtil.R.serverError;
import static org.opencastproject.util.doc.rest.RestParameter.Type.BOOLEAN;
import static org.opencastproject.util.doc.rest.RestParameter.Type.STRING;

import org.opencastproject.adopter.statistic.ScheduledDataCollector;
import org.opencastproject.util.doc.rest.RestParameter;
import org.opencastproject.util.doc.rest.RestQuery;
import org.opencastproject.util.doc.rest.RestResponse;
import org.opencastproject.util.doc.rest.RestService;

import com.google.gson.Gson;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The REST endpoint for the adopter statistics service.
 */
@Path("/admin-ng/adopter")
@RestService(name = "registrationController",
        title = "Adopter Statistics Registration Service Endpoint",
        abstractText = "Rest Endpoint for the registration form.",
        notes = {"Provides operations regarding the adopter registration form"})
@Component(
    immediate = true,
    service = Controller.class,
    property = {
        "service.description=Adopter Statistics REST Endpoint",
        "opencast.service.type=org.opencastproject.adopter.registration.Controller",
        "opencast.service.path=/admin-ng/adopter",
        "opencast.service.jobproducer=false"
    }
)
@JaxrsResource
public class Controller {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(Controller.class);

  /** The JSON parser */
  private static final Gson gson = new Gson();

  /** The rest docs */
  protected String docs;

  /** The service that provides methods for the registration */
  protected Service registrationService;

  /** The scheduled data collector so we can pull the current stats on demand */
  protected ScheduledDataCollector dataCollector;

  /** OSGi setter for the registration service */
  @Reference
  public void setRegistrationService(Service registrationService) {
    this.registrationService = registrationService;
  }

  @Reference
  public void setDataCollector(ScheduledDataCollector collector) {
    this.dataCollector = collector;
  }

  @GET
  @Path("registration")
  @Produces(MediaType.APPLICATION_JSON)
  @RestQuery(name = "getregistrationform", description = "GETs the adopter registration data.", responses = {
          @RestResponse(description = "Retrieved registration data.",
                        responseCode = HttpServletResponse.SC_OK),
          @RestResponse(description = "Error while retrieving adopter registration data.",
                        responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR) },
                        returnDescription = "GETs the adopter registration data.")
  public String getRegistrationForm() {
    logger.debug("Retrieving adopter registration data.");
    return gson.toJson(registrationService.retrieveFormData());
  }

  @GET
  @Path("summary")
  @Produces(MediaType.APPLICATION_JSON)
  @RestQuery(name = "getsummary", description = "GETs the adopter registration statistics data.", responses = {
      @RestResponse(description = "Retrieved statistic data.",
          responseCode = HttpServletResponse.SC_OK),
      @RestResponse(description = "Error while retrieving adopter statistic data.",
          responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR) },
      returnDescription = "GETs the adopter registration statistics data.")
  public Response getAdopterStatistics() {
    logger.debug("Retrieving adopter registration statistics data.");
    try {
      return Response.ok(dataCollector.getRegistrationDataAsString()).build();
    } catch (Exception e) {
      return Response.serverError().build();
    }
  }

  @POST
  @Path("registration")
  @RestQuery(name = "saveregistrationform",
          description = "Saves the adopter registration data.",
          returnDescription = "Status",
          restParameters = {
                  @RestParameter(description = "The Name of the organisation.",
                          isRequired = false, name = "organisationName", type = STRING),
                  @RestParameter(description = "The Name of the department.",
                          isRequired = false, name = "departmentName", type = STRING),
                  @RestParameter(description = "The First name.",
                          isRequired = false, name = "firstName", type = STRING),
                  @RestParameter(description = "The Last name.",
                          isRequired = false, name = "lastName", type = STRING),
                  @RestParameter(description = "The e-mail address.",
                          isRequired = false, name = "email", type = STRING),
                  @RestParameter(description = "The country.",
                          isRequired = false, name = "country", type = STRING),
                  @RestParameter(description = "The postal code.",
                          isRequired = false, name = "postalCode", type = STRING),
                  @RestParameter(description = "The city.",
                          isRequired = false, name = "city", type = STRING),
                  @RestParameter(description = "The street.",
                          isRequired = false, name = "street", type = STRING),
                  @RestParameter(description = "The street number.",
                          isRequired = false, name = "streetNo", type = STRING),
                  @RestParameter(description = "Does the adopter allows to be contacted.",
                          isRequired = false, name = "contactMe", type = BOOLEAN),
                  @RestParameter(description = "Does the adopter agreed to the policy.",
                          isRequired = false, name = "agreedToPolicy", type = BOOLEAN),
                  @RestParameter(description = "Does the adopter allow the gathering of error reports.",
                          isRequired = false, name = "allowsErrorReports", type = BOOLEAN),
                  @RestParameter(description = "Which type of system is this.",
                          isRequired = false, name = "systemType", type = STRING),
                  @RestParameter(description = "Does the adopter allow the gathering of statistic data.",
                          isRequired = false, name = "allowsStatistics", type = BOOLEAN),
                  @RestParameter(description = "Is the adopter already registered.",
                          isRequired = false, name = "registered", type = BOOLEAN)
          },
          responses = {
          @RestResponse(responseCode = SC_OK, description = "Adopter registration data saved."),
          @RestResponse(responseCode = SC_BAD_REQUEST, description = "Couldn't save adopter registration data.")})
  public Response register(
          @FormParam("organisationName") String organisationName,
          @FormParam("departmentName") String departmentName,
          @FormParam("firstName") String firstName,
          @FormParam("lastName") String lastName,
          @FormParam("email") String email,
          @FormParam("country") String country,
          @FormParam("postalCode") String postalCode,
          @FormParam("city") String city,
          @FormParam("street") String street,
          @FormParam("streetNo") String streetNo,
          @FormParam("contactMe") boolean contactMe,
          @FormParam("agreedToPolicy") boolean agreedToPolicy,
          @FormParam("systemType") String systemType,
          @FormParam("allowsErrorReports") boolean allowsErrorReports,
          @FormParam("allowsStatistics") boolean allowsStatistics,
          @FormParam("registered") boolean registered) {
    logger.debug("Saving adopter registration data.");

    Form form = new Form(organisationName, departmentName, firstName, lastName, email, country, postalCode, city,
            street, streetNo, contactMe, systemType, allowsStatistics, allowsErrorReports, agreedToPolicy, registered
    );
    try {
      registrationService.saveFormData(form);
    } catch (Exception e) {
      logger.error("Error while saving adopter registration data.", e);
      return Response.serverError().build();
    }
    return Response.ok().build();
  }

  @POST
  @Path("registration/finalize")
  @RestQuery(name = "finalizeRegistration",
      description = "Finalizes the registration and starts sending data.",
      returnDescription = "Status",
      restParameters = {},
      responses = { @RestResponse(responseCode = SC_OK, description = "Registration finalized.") })
  public Response register() {
    logger.debug("Finalizing adopter registration.");

    Form form = (Form) registrationService.retrieveFormData();
    form.setRegistered(true);

    try {
      registrationService.saveFormData(form);
    } catch (Exception e) {
      logger.error("Error while saving adopter registration data.", e);
      return Response.serverError().build();
    }
    return Response.ok().build();
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("isUpToDate")
  @RestQuery(name = "isUpToDate", description = "Returns true if Opencast has been able to register", responses = {
      @RestResponse(description = "Registratino status",
          responseCode = HttpServletResponse.SC_OK)
      },
      returnDescription = "true if registration has been updated in the last week, false otherwise")
  public Response isUpToDate() {
    Form data = (Form) registrationService.retrieveFormData();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, -7);
    //A fresh install might have no data at all, so we null check everything
    if (data != null && data.getDateModified() != null && data.getDateModified().after(cal.getTime())) {
      return Response.ok("true").build();
    }
    return Response.ok("false").build();
  }

  @DELETE
  @Path("registration")
  @RestQuery(name = "deleteregistrationform", description = "Deletes the adopter registration data", responses = {
          @RestResponse(description = "Successful deleted form data.",
                  responseCode = HttpServletResponse.SC_OK),
          @RestResponse(description = "Error while deleting adopter registration data.",
                  responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR) },
          returnDescription = "DELETEs the adopter registration data.")
  public Response deleteRegistrationData() {
    logger.debug("Deleting adopter registration data.");
    try {
      registrationService.markForDeletion();
      return ok();
    } catch (Exception e) {
      logger.error("Error while deleting adopter registration data.", e);
      return serverError();
    }
  }


  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("latestToU")
  @RestQuery(name = "getLatestTermsOfUse", description = "Gets the latest terms of use version.", responses = {
      @RestResponse(description = "Retrieved statistic data.", responseCode = HttpServletResponse.SC_OK) },
      returnDescription = "The latest terms of use version.")
  public String getLatestTermsofUse() {
    return Form.getLatestTermsOfUse().name();
  }


  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("docs")
  public String getDocs() {
    return docs;
  }

}
