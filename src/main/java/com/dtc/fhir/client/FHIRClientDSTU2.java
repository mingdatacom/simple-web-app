package com.dtc.fhir.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.hl7.fhir.instance.model.api.IIdType;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.IValueSetEnumBinder;
import ca.uhn.fhir.model.dstu2.composite.AddressDt;
import ca.uhn.fhir.model.dstu2.composite.AnnotationDt;
import ca.uhn.fhir.model.dstu2.composite.BoundCodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ContainedDt;
import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.RatioDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.composite.TimingDt;
import ca.uhn.fhir.model.dstu2.resource.AuditEvent;
import ca.uhn.fhir.model.dstu2.resource.AuditEvent.Event;
import ca.uhn.fhir.model.dstu2.resource.AuditEvent.ObjectElement;
import ca.uhn.fhir.model.dstu2.resource.AuditEvent.Source;
import ca.uhn.fhir.model.dstu2.resource.Binary;
import ca.uhn.fhir.model.dstu2.resource.Communication;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Conformance;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticReport;
import ca.uhn.fhir.model.dstu2.resource.DocumentReference;
import ca.uhn.fhir.model.dstu2.resource.DocumentReference.Context;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.ImagingObjectSelection;
import ca.uhn.fhir.model.dstu2.resource.ImagingStudy;
import ca.uhn.fhir.model.dstu2.resource.ListResource;
import ca.uhn.fhir.model.dstu2.resource.Medication;
import ca.uhn.fhir.model.dstu2.resource.Medication.Product;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder.DispenseRequest;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Organization;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.resource.Procedure;
import ca.uhn.fhir.model.dstu2.resource.Specimen;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.AuditEventActionEnum;
import ca.uhn.fhir.model.dstu2.valueset.AuditEventOutcomeEnum;
import ca.uhn.fhir.model.dstu2.valueset.ConditionCategoryCodesEnum;
import ca.uhn.fhir.model.dstu2.valueset.ConditionClinicalStatusCodesEnum;
import ca.uhn.fhir.model.dstu2.valueset.DiagnosticReportStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.DocumentReferenceStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.EncounterClassEnum;
import ca.uhn.fhir.model.dstu2.valueset.EncounterStateEnum;
import ca.uhn.fhir.model.dstu2.valueset.ListModeEnum;
import ca.uhn.fhir.model.dstu2.valueset.MaritalStatusCodesEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationOrderStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.ObservationStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.OrganizationTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.PractitionerRoleEnum;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IParam;
import ca.uhn.fhir.rest.gclient.IQuery;

/**
 * The <code>FHIRClient.java</code> class.
 * 
 * @version $Name: $, $Revision: 1.0 $, $Date: 2015/8/14 $
 * @author <a href="mailto:leesh@datacom.com.tw">Siang Hao Lee</a>
 */

public class FHIRClientDSTU2 {
    private static String connectUrl = null;

    private static String userAgent = null;

    private static IGenericClient client;

    private Organization defaultOrg;

    private Patient defaultPatient;

    private Practitioner defaultPractitioner;

    private byte[] defaultBinary;

    private static void configureForLocal() {
        // connectUrl = "http://spark.furore.com/fhir/";
        // connectUrl = "http://fhir.healthintersections.com.au/open";
        // connectUrl = "http://fhirtest.uhn.ca/base";
        
        //DSTU1
        //connectUrl = "http://192.168.2.103:8000/hapi-fhir-jpaserver/base";
        //DSTU2
        //connectUrl = "http://192.168.2.101:8000/fhir-jpaserver/base";
        connectUrl = "http://192.168.1.21:8088/baseDstu2";
        userAgent = "HAPI FHIR Server";
    }

    public FHIRClientDSTU2() {
        configureForLocal();
        init();
    }

    public FHIRClientDSTU2(String baseURI) {
        connectUrl = baseURI;
        init();
    }

    public FHIRClientDSTU2(String baseURI, String userAgentName) {
        connectUrl = baseURI;
        userAgent = userAgentName;
        init();
    }

    private Organization readDefaultOrganization() {
        Organization org = client.read(Organization.class, new IdDt(connectUrl + "/Organization/1.2.840.269649"));
        System.out.println("Default Organization ID:" + org.getId());
        return org;
    }

    private Patient readDefaultPatient() {
        Patient patient = client.read(Patient.class, new IdDt(connectUrl + "/Patient/A123456789"));
        System.out.println("Default Patient ID:" + patient.getId());
        return patient;
    }

    private Practitioner readDefaultPractitioner() {
        Practitioner practitioner = client.read(Practitioner.class, new IdDt(connectUrl + "/Practitioner/BA00243738"));
        System.out.println("Default Practitioner ID:" + practitioner.getId());
        return practitioner;
    }

    public static void init() {
        try {
            FhirContext ctx = new FhirContext().forDstu2();
            ctx.getRestfulClientFactory().setConnectionRequestTimeout(120000);
            ctx.getRestfulClientFactory().setConnectTimeout(120000);
            ctx.getRestfulClientFactory().setSocketTimeout(120000);
            client = ctx.newRestfulGenericClient(connectUrl);
            LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
            loggingInterceptor.setLogRequestSummary(true);
            // loggingInterceptor.setLogRequestBody(true);
            client.registerInterceptor(loggingInterceptor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean FHIRSimpleClient() {
        try {
            Conformance c = client.fetchConformance().ofType(Conformance.class).execute();
            if (c.getDescriptionElement().getValue().isEmpty())
                return true;
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getConformanceStatement() {
        String result = null;
        try {
            Conformance c = client.fetchConformance().ofType(Conformance.class).execute();
            if (c != null && !c.getDescriptionElement().isEmpty())
                result = c.getDescriptionElement().getValue();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
    }

    public String createResource(IResource resourceClass) {
        String result = null;
        try {
            MethodOutcome mo = client.create().resource(resourceClass).prettyPrint().encodedJson().execute();
            if (mo.getCreated())
                result = mo.getId().getValue();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
    }

    public IIdType createResourceReturnId(IResource resourceClass) {
        IIdType result = null;
        try {
            MethodOutcome mo = client.create().resource(resourceClass).prettyPrint().encodedJson().execute();
            if (mo.getCreated())
                result = mo.getId();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
    }

    public <T extends IResource> IResource readResource(Class<T> resourceClass, String resourceId) {
        try {
            return client.read(resourceClass, new IdDt(connectUrl + "/" + resourceClass.getSimpleName() + "/" + resourceId));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T extends IResource> String updateResource(Class<T> resourceClass, String targetIdentifier, IResource updateResource) {
        String updateResult = null;
        IResource targetResource = readResource(resourceClass, targetIdentifier);
        updateResource.setId(targetResource.getId());
        if (!updateResource.equals(targetResource)) {
            //MethodOutcome mo = client.update().resource(updateResource).execute();
            MethodOutcome mo = client.update(targetIdentifier, updateResource);
            if (mo != null)
                updateResult = mo.getId().getValue();
        }
        return updateResult;
    }

    public <T extends IResource> IIdType updateResourceReturnId(Class<T> resourceClass, String targetIdentifier,
            IResource updateResource) {
        IIdType updateResult = null;
        IResource targetResource = readResource(resourceClass, targetIdentifier);
        updateResource.setId(targetResource.getId());
        if (!updateResource.equals(targetResource)) {
            MethodOutcome mo = client.update().resource(updateResource).execute();
            if (mo != null)
                updateResult = mo.getId();
        }
        return updateResult;
    }

    public <T extends IResource, U extends IParam> List<T> searchResource(Class<T> resourceClass, ICriterion<U> parameter) {
        List<T> resultList = null;
        Bundle bundle = client.search().forResource(resourceClass).where(parameter).prettyPrint().encodedJson().execute();
        // System.out.println(resourceClass.getName() + " search result:" + bundle.getTotalResults());
        if (bundle.getTotalResults().getValue() > 0) {
            resultList = bundle.getResources(resourceClass);
            // System.out.println(resourceClass.getName() + " return resource:" + resultList.size());
        }
        return resultList;
    }

    public <T extends IResource> List<T> searchResourceByParams(Class<T> resourceClass, List<ICriterion> parameters) {
        List<T> resultList = null;
        if (parameters.size() > 0) {
            IQuery<Bundle> query = client.search().forResource(resourceClass).where(parameters.get(0));
            if (parameters.size() > 1) {
                for (int i = 1; i < parameters.size(); i++)
                    query.and(parameters.get(i));
            }
            Bundle bundle = query.prettyPrint().encodedJson().execute();
            // System.out.println(resourceClass.getName() + " search result:" + bundle.getTotalResults());
            if (bundle.getTotalResults().getValue() > 0) {
                resultList = bundle.getResources(resourceClass);
                // System.out.println(resourceClass.getName() + " return resource:" + resultList.size());
            }
        }
        return resultList;
    }

    public <T extends IResource, U extends IParam> List<T> searchResourceAll(Class<T> resourceClass, ICriterion<U> parameter) {
        List<T> resultList = null;
        Bundle bundle = client.search().forResource(resourceClass).where(parameter).prettyPrint().encodedJson().execute();
        // System.out.println(resourceClass.getName() + " search result:" + bundle.getTotalResults());
        if (bundle.getTotalResults().getValue() > 0) {
            resultList = new ArrayList<T>();
            resultList.addAll(bundle.getResources(resourceClass));
            while (!bundle.getLinkNext().isEmpty()) {
                setFhirBase(bundle);
                bundle = client.loadPage().next(bundle).execute();
                resultList.addAll(bundle.getResources(resourceClass));
            }
            // System.out.println(resourceClass.getName() + " return resource:" + resultList.size());
        }
        return resultList;
    }

    public <T extends IResource> List<T> searchResourceByParamsAll(Class<T> resourceClass, List<ICriterion> parameters) {
        List<T> resultList = null;
        if (parameters.size() > 0) {
            IQuery<Bundle> query = client.search().forResource(resourceClass).where(parameters.get(0));
            if (parameters.size() > 1) {
                for (int i = 1; i < parameters.size(); i++)
                    query.and(parameters.get(i));
            }
            Bundle bundle = query.prettyPrint().encodedJson().execute();
            // System.out.println(resourceClass.getName() + " search result:" + bundle.getTotalResults());
            if (bundle.getTotalResults().getValue() > 0) {
                resultList = new ArrayList<T>();
                resultList.addAll(bundle.getResources(resourceClass));
                while (!bundle.getLinkNext().isEmpty()) {
                    setFhirBase(bundle);
                    bundle = client.loadPage().next(bundle).execute();
                    resultList.addAll(bundle.getResources(resourceClass));
                }
                // System.out.println(resourceClass.getName() + " return resource:" + resultList.size());
            }
        }
        return resultList;
    }

    public String createDefaultOrganization() {
        return createResource(createDTCOrganization());
    }

    public String createOrganization(Organization org) {
        return createResource(org);
    }

    public Organization readOrganization(String id) {
        return (Organization) readResource(Organization.class, id);
    }

    public String updateOrganization(String targetId, Organization updateOrg) {
        String updateResult = null;
        MethodOutcome mo = client.update(targetId, updateOrg);
        if (mo != null)
            updateResult = mo.getId().getValue();
        return updateResult;
        //return updateResource(Organization.class, targetId, updateOrg);
    }

    public List<Organization> searchOrganizationById(String id) {
        return searchResource(Organization.class, Organization.RES_ID.matchesExactly().value(id));
    }

    public List<Organization> searchOrganizationByIdentifier(String identifier) {
        return searchResource(Organization.class, Organization.IDENTIFIER.exactly().code(identifier));
    }

    public List<Organization> searchOrganizationByParams(String idSystem, String identifier, String name) {
        List<ICriterion> paramlist = new ArrayList<ICriterion>();
        paramlist.add(Organization.IDENTIFIER.exactly().systemAndIdentifier(idSystem, identifier));
        paramlist.add(Organization.NAME.matches().value(name));
        return searchResourceByParamsAll(Organization.class, paramlist);
    }

    public String createDefaultPractitioner() {
        return createResource(createDTCPractitioner());
    }

    public String createPractitioner(Practitioner practitioner) {
        return createResource(practitioner);
    }

    public Practitioner readPractitioner(String id) {
        return (Practitioner) readResource(Practitioner.class, id);
    }

    public String updatePractitioner(String targetId, Practitioner updatePractitioner) {
        return updateResource(Practitioner.class, targetId, updatePractitioner);
    }

    public List<Practitioner> searchPractitionerById(String id) {
        return searchResource(Practitioner.class, Practitioner.RES_ID.matchesExactly().value(id));
    }

    public List<Practitioner> searchPractitionerByIdentifier(String identifier) {
        return searchResource(Practitioner.class, Practitioner.IDENTIFIER.exactly().code(identifier));
    }

    public List<Practitioner> searchPractitionerByParams(String name, String tel, String email, String orgName) {
        List<ICriterion> paramlist = new ArrayList<ICriterion>();
        if (name != null && name.trim().length() > 0)
            paramlist.add(Practitioner.NAME.matches().value(name));
        if (tel != null && tel.trim().length() > 0)
            paramlist.add(Practitioner.PHONETIC.matches().value(tel));
        if (email != null && email.trim().length() > 0)
            paramlist.add(Practitioner.ADDRESS.matches().value(email));
        if (orgName != null && orgName.trim().length() > 0)
            paramlist.add(Practitioner.ORGANIZATION.hasChainedProperty(Organization.NAME.matches().value(orgName)));
        return searchResourceByParamsAll(Practitioner.class, paramlist);
    }

    public String createAuditEvent(AuditEvent auditEvent) {
        return createResource(auditEvent);
    }

    public List<AuditEvent> searchAuditEventByDate(Date startDate, Date endDate) {
        List<ICriterion> paramlist = new ArrayList<ICriterion>();
        if (startDate != null)
            paramlist.add(AuditEvent.DATE.afterOrEquals().day(startDate));
        if (endDate != null)
            paramlist.add(AuditEvent.DATE.beforeOrEquals().day(endDate));
        return searchResourceByParamsAll(AuditEvent.class, paramlist);
    }

    public String createPatient(Patient patient) {
        return createResource(patient);
    }

    public Patient readPatient(String id) {
        return (Patient) readResource(Patient.class, id);
    }

    public String updatePatient(String targetId, Patient updatePatient) {
        return updateResource(Patient.class, targetId, updatePatient);
    }

    public List<Patient> searchPatientById(String id) {
        return searchResource(Patient.class, Patient.RES_ID.matchesExactly().value(id));
    }

    public List<Patient> searchPatientByIdentifier(String identifier) {
        return searchResource(Patient.class, Patient.IDENTIFIER.exactly().code(identifier));
    }

    public List<Patient> searchPatientByParams(String name, AdministrativeGenderEnum sex, Date birthDate, String tel,
            String addr, String email) {
        List<ICriterion> paramlist = new ArrayList<ICriterion>();
        if (name != null && name.trim().length() > 0)
            paramlist.add(Patient.NAME.matches().value(name));
        if (sex != null)
            paramlist.add(Patient.GENDER.exactly().code(sex.getCode()));
        if (birthDate != null)
            paramlist.add(Patient.BIRTHDATE.exactly().day(birthDate));
        if (tel != null && tel.trim().length() > 0)
            paramlist.add(Patient.PHONETIC.matches().value(tel));
        if (addr != null && addr.trim().length() > 0)
            paramlist.add(Patient.ADDRESS.matches().value(addr));
        if (email != null && email.trim().length() > 0)
            paramlist.add(Patient.LINK.hasId(email));
        return searchResourceByParamsAll(Patient.class, paramlist);
    }

    public String createBinary(Binary binary) {
        return createResource(binary);
    }

    public Binary readBinary(String id) {
        return (Binary) readResource(Binary.class, id);
    }

    /*
     * public String updateBinary(String targetId, Patient updateBinary) { return updateResource(Binary.class, targetId,
     * updateBinary); }
     */

    public List<Binary> searchBinaryById(String id) {
        return searchResource(Binary.class, Binary.RES_ID.matchesExactly().value(id));
    }

    public String createDocumentReference(DocumentReference docRef) {
        return createResource(docRef);
    }

    public DocumentReference readDocumentReference(String id) {
        return (DocumentReference) readResource(DocumentReference.class, id);
    }

    public String updateDocumentReference(String targetId, DocumentReference updateDocRef) {
        return updateResource(DocumentReference.class, targetId, updateDocRef);
    }

    public IIdType updateDocumentReferenceReturnId(String targetId, DocumentReference updateDocRef) {
        return updateResourceReturnId(DocumentReference.class, targetId, updateDocRef);
    }

    public List<DocumentReference> searchDocumentReferenceById(String id) {
        return searchResource(DocumentReference.class, DocumentReference.RES_ID.matchesExactly().value(id));
    }

    public List<DocumentReference> searchDocumentReferenceByIdentifier(String identifier) {
        return searchResource(DocumentReference.class, DocumentReference.IDENTIFIER.exactly().code(identifier));
    }

    public List<DocumentReference> searchDocumentReferenceByParams(String authorId, String authorName, String custodianId,
            String custodianName, String classCode, String typeCode, String facilityCode, String formatCode,
            String confidentialityCode, String eventCode, String statusCode, String recordTargetId, String repositoryURI,
            Date serviceStartDate, Date serviceEndDate) {
        List<ICriterion> paramlist = new ArrayList<ICriterion>();
        if (authorId != null && authorId.trim().length() > 0)
            paramlist.add(DocumentReference.AUTHOR.hasId(authorId));
        if (authorName != null && authorName.trim().length() > 0)
            paramlist.add(DocumentReference.AUTHOR.hasChainedProperty(Practitioner.NAME.matches().value(authorName)));
        if (custodianId != null && custodianId.trim().length() > 0)
            paramlist.add(DocumentReference.CUSTODIAN.hasId(custodianId));
        if (custodianName != null && custodianName.trim().length() > 0)
            paramlist.add(DocumentReference.CUSTODIAN.hasChainedProperty(Organization.NAME.matches().value(custodianName)));
        if (classCode != null && classCode.trim().length() > 0)
            paramlist.add(DocumentReference.CLASS.exactly().code(classCode));
        if (typeCode != null && typeCode.trim().length() > 0)
            paramlist.add(DocumentReference.TYPE.exactly().code(typeCode));
        if (facilityCode != null && facilityCode.trim().length() > 0)
            paramlist.add(DocumentReference.FACILITY.exactly().code(facilityCode));
        if (formatCode != null && formatCode.trim().length() > 0)
            paramlist.add(DocumentReference.FORMAT.exactly().code(formatCode));
        if (confidentialityCode != null && confidentialityCode.trim().length() > 0)
            paramlist.add(DocumentReference.SECURITYLABEL.exactly().code(confidentialityCode));
        if (eventCode != null && eventCode.trim().length() > 0)
            paramlist.add(DocumentReference.EVENT.exactly().code(eventCode));
        if (statusCode != null && statusCode.trim().length() > 0)
            paramlist.add(DocumentReference.STATUS.exactly().code(statusCode));
        if (recordTargetId != null && recordTargetId.trim().length() > 0)
            paramlist.add(DocumentReference.SUBJECT.hasId(recordTargetId));
        if (repositoryURI != null && repositoryURI.trim().length() > 0)
            paramlist.add(DocumentReference.LOCATION.matches(repositoryURI).value(repositoryURI));
        if (serviceStartDate != null)
            paramlist.add(DocumentReference.PERIOD.afterOrEquals().day(serviceStartDate));
        if (serviceEndDate != null)
            paramlist.add(DocumentReference.PERIOD.beforeOrEquals().day(serviceEndDate));
        // TODO: service (BinaryId) URI
        return searchResourceByParamsAll(DocumentReference.class, paramlist);
    }

    public String createList(ListResource list) {
        return createResource(list);
    }

    public ListResource readList(String id) {
        return (ListResource) readResource(ListResource.class, id);
    }

    public String updateList(String targetId, ListResource updateList) {
        return updateResource(ListResource.class, targetId, updateList);
    }

    public IIdType updateListReturnId(String targetId, ListResource updateList) {
        return updateResourceReturnId(ListResource.class, targetId, updateList);
    }

    public List<ListResource> searchListById(String id) {
        return searchResource(ListResource.class, ListResource.RES_ID.matchesExactly().value(id));
    }

    public List<ListResource> searchListByParams(String listCode, String sourceId, String subjectId, Date listCreateStartDate,
            Date listCreateEndDate) {
        List<ICriterion> paramlist = new ArrayList<ICriterion>();
        if (listCode != null && listCode.trim().length() > 0)
            paramlist.add(ListResource.CODE.exactly().code(listCode));
        if (subjectId != null && subjectId.trim().length() > 0)
            paramlist.add(ListResource.SUBJECT.hasId(subjectId));
        if (sourceId != null && sourceId.trim().length() > 0)
            paramlist.add(ListResource.SOURCE.hasId(sourceId));
        if (listCreateStartDate != null)
            paramlist.add(ListResource.DATE.afterOrEquals().day(listCreateStartDate));
        if (listCreateEndDate != null)
            paramlist.add(ListResource.DATE.beforeOrEquals().day(listCreateEndDate));
        return searchResourceByParamsAll(ListResource.class, paramlist);
    }
    
    public String createDiagnosticReport(DiagnosticReport diagnosticReport) {
        return createResource(diagnosticReport);
    }

    public DiagnosticReport readDiagnosticReport(String id) {
        return (DiagnosticReport) readResource(DiagnosticReport.class, id);
    }

    public String updateDiagnosticReport(String targetId, DiagnosticReport updateDiagnosticReport) {
        return updateResource(DiagnosticReport.class, targetId, updateDiagnosticReport);
    }

    public IIdType updateDiagnosticReportReturnId(String targetId, DiagnosticReport updateDiagnosticReport) {
        return updateResourceReturnId(DiagnosticReport.class, targetId, updateDiagnosticReport);
    }

    public List<DiagnosticReport> searchDiagnosticReportById(String id) {
        return searchResource(DiagnosticReport.class, DiagnosticReport.RES_ID.matchesExactly().value(id));
    }

    public List<DiagnosticReport> searchDiagnosticReportByParams(Date date, String codedDiagnosis, String identifier,
            String sourceId, String subjectId, String performerId, String serviceCategory, String status,
            Date createStartDate, Date createEndDate) {
        List<ICriterion> paramlist = new ArrayList<ICriterion>();
        if (codedDiagnosis != null && codedDiagnosis.trim().length() > 0)
            paramlist.add(DiagnosticReport.DIAGNOSIS.exactly().code(codedDiagnosis));
        if (identifier != null && identifier.trim().length() > 0)
            paramlist.add(DiagnosticReport.IDENTIFIER.exactly().code(identifier));
        if (createStartDate != null)
            paramlist.add(DiagnosticReport.ISSUED.afterOrEquals().day(createStartDate));
        if (createEndDate != null)
            paramlist.add(DiagnosticReport.ISSUED.beforeOrEquals().day(createEndDate));
        if (subjectId != null && subjectId.trim().length() > 0)
            paramlist.add(DiagnosticReport.SUBJECT.hasId(subjectId));
        if (subjectId != null && subjectId.trim().length() > 0)
            paramlist.add(DiagnosticReport.PATIENT.hasId(subjectId));
        if (performerId != null && performerId.trim().length() > 0)
            paramlist.add(DiagnosticReport.PERFORMER.hasId(performerId));
        if (serviceCategory != null && serviceCategory.trim().length() > 0)
            paramlist.add(DiagnosticReport.CATEGORY.exactly().code(serviceCategory));
        if (status != null && status.trim().length() > 0)
            paramlist.add(DiagnosticReport.STATUS.exactly().code(status));
        /*if (createStartDate != null)
            paramlist.add(DiagnosticReport.DATE.afterOrEquals().day(createStartDate));
        if (createEndDate != null)
            paramlist.add(DiagnosticReport.DATE.beforeOrEquals().day(createEndDate));*/
        return searchResourceByParamsAll(DiagnosticReport.class, paramlist);
    }
    
    public String createDiagnosticOrder(DiagnosticOrder diagnosticOrder) {
        return createResource(diagnosticOrder);
    }

    public DiagnosticOrder readDiagnosticOrder(String id) {
        return (DiagnosticOrder) readResource(DiagnosticOrder.class, id);
    }

    public String updateDiagnosticOrder(String targetId, DiagnosticOrder updateDiagnosticOrder) {
        return updateResource(DiagnosticOrder.class, targetId, updateDiagnosticOrder);
    }

    public IIdType updateDiagnosticOrderReturnId(String targetId, DiagnosticOrder updateDiagnosticOrder) {
        return updateResourceReturnId(DiagnosticOrder.class, targetId, updateDiagnosticOrder);
    }

    public List<DiagnosticOrder> searchDiagnosticOrderById(String id) {
        return searchResource(DiagnosticOrder.class, DiagnosticOrder.RES_ID.matchesExactly().value(id));
    }

    public List<DiagnosticOrder> searchDiagnosticOrderByParams(Date date, String actorRefId, String bodySiteCode, 
            String orderItemCode, String encounterRefId, Date orderStartDate, Date orderEndDate, String eventStatus,
            String identifier, String ordererRefId, String specimenRefId, String status, String subjectRefId) {
        List<ICriterion> paramlist = new ArrayList<ICriterion>();
        if (actorRefId != null && actorRefId.trim().length() > 0)
            paramlist.add(DiagnosticOrder.ACTOR.hasId(actorRefId));
        if (bodySiteCode != null && bodySiteCode.trim().length() > 0)
            paramlist.add(DiagnosticOrder.BODYSITE.exactly().code(bodySiteCode));
        if (orderItemCode != null && orderItemCode.trim().length() > 0)
            paramlist.add(DiagnosticOrder.CODE.exactly().code(orderItemCode));
        if (encounterRefId != null && encounterRefId.trim().length() > 0)
            paramlist.add(DiagnosticOrder.ENCOUNTER.hasId(encounterRefId));
        if (orderStartDate != null)
            paramlist.add(DiagnosticOrder.EVENT_DATE.afterOrEquals().day(orderStartDate));
        if (orderEndDate != null)
            paramlist.add(DiagnosticOrder.EVENT_DATE.beforeOrEquals().day(orderEndDate));
        if (eventStatus != null && eventStatus.trim().length() > 0)
            paramlist.add(DiagnosticOrder.EVENT_STATUS.exactly().code(eventStatus));
        if (identifier != null && identifier.trim().length() > 0)
            paramlist.add(DiagnosticOrder.IDENTIFIER.exactly().code(identifier));
        if (ordererRefId != null && ordererRefId.trim().length() > 0)
            paramlist.add(DiagnosticOrder.ORDERER.hasId(ordererRefId));
        if (specimenRefId != null && specimenRefId.trim().length() > 0)
            paramlist.add(DiagnosticOrder.SPECIMEN.hasId(specimenRefId));
        if (subjectRefId != null && subjectRefId.trim().length() > 0)
            paramlist.add(DiagnosticOrder.SUBJECT.hasId(subjectRefId));
        if (status != null && status.trim().length() > 0)
            paramlist.add(DiagnosticOrder.STATUS.exactly().code(status));
        return searchResourceByParamsAll(DiagnosticOrder.class, paramlist);
    }
    
    public String createImagingStudy(ImagingStudy imagingStudy) {
        return createResource(imagingStudy);
    }

    public ImagingStudy readImagingStudy(String id) {
        return (ImagingStudy) readResource(ImagingStudy.class, id);
    }

    public String updateImagingStudy(String targetId, ImagingStudy updateImagingStudy) {
        return updateResource(ImagingStudy.class, targetId, updateImagingStudy);
    }

    public IIdType updateImagingStudyReturnId(String targetId, ImagingStudy updateImagingStudy) {
        return updateResourceReturnId(ImagingStudy.class, targetId, updateImagingStudy);
    }

    public List<ImagingStudy> searchImagingStudyById(String id) {
        return searchResource(ImagingStudy.class, ImagingStudy.RES_ID.matchesExactly().value(id));
    }

    public List<ImagingStudy> searchImagingStudyByParams(String accession, String bodySite,
            String dicomClass, String modality, String orderId, String patientId, String series, String study,
            String uid, Date studyStartDate, Date studyEndDate) {
        List<ICriterion> paramlist = new ArrayList<ICriterion>();
        if (accession != null && accession.trim().length() > 0)
            paramlist.add(ImagingStudy.ACCESSION.exactly().code(accession));
        if (bodySite != null && bodySite.trim().length() > 0)
            paramlist.add(ImagingStudy.BODYSITE.exactly().code(bodySite));
        if (dicomClass != null && dicomClass.trim().length() > 0)
            paramlist.add(ImagingStudy.DICOM_CLASS.matches(dicomClass).value(dicomClass));
        if (modality != null && modality.trim().length() > 0)
            paramlist.add(ImagingStudy.MODALITY.exactly().code(modality));
        if (orderId != null && orderId.trim().length() > 0)
            paramlist.add(ImagingStudy.ORDER.hasId(orderId));
        if (patientId != null && patientId.trim().length() > 0)
            paramlist.add(ImagingStudy.PATIENT.hasId(patientId));
        if (series != null && series.trim().length() > 0)
            paramlist.add(ImagingStudy.SERIES.matches(series).value(series));
        if (study != null && study.trim().length() > 0)
            paramlist.add(ImagingStudy.STUDY.matches(study).value(study));
        if (uid != null && uid.trim().length() > 0)
            paramlist.add(ImagingStudy.UID.matches(uid).value(uid));
        if (studyStartDate != null)
            paramlist.add(ImagingStudy.STARTED.afterOrEquals().day(studyStartDate));
        if (studyEndDate != null)
            paramlist.add(ImagingStudy.STARTED.beforeOrEquals().day(studyEndDate));
        return searchResourceByParamsAll(ImagingStudy.class, paramlist);
    }
    
    public String createImagingObjectSelection(ImagingObjectSelection imagingObjectSelection) {
        return createResource(imagingObjectSelection);
    }

    public ImagingObjectSelection readImagingObjectSelection(String id) {
        return (ImagingObjectSelection) readResource(ImagingObjectSelection.class, id);
    }

    public String updateImagingObjectSelection(String targetId, ImagingObjectSelection updateImagingObjectSelection) {
        return updateResource(ImagingObjectSelection.class, targetId, updateImagingObjectSelection);
    }

    public IIdType updateImagingObjectSelectionReturnId(String targetId, ImagingObjectSelection updateImagingObjectSelection) {
        return updateResourceReturnId(ImagingObjectSelection.class, targetId, updateImagingObjectSelection);
    }

    public List<ImagingObjectSelection> searchImagingObjectSelectionById(String id) {
        return searchResource(ImagingObjectSelection.class, ImagingObjectSelection.RES_ID.matchesExactly().value(id));
    }

    public List<ImagingObjectSelection> searchImagingObjectSelectionByParams(String authorId, String identifier,
            String patientId, String selectedStudy, String title, Date authoringStartDate, Date authoringEndDate) {
        List<ICriterion> paramlist = new ArrayList<ICriterion>();
        if (authorId != null && authorId.trim().length() > 0)
            paramlist.add(ImagingObjectSelection.AUTHOR.hasId(authorId));
        if (identifier != null && identifier.trim().length() > 0)
            paramlist.add(ImagingObjectSelection.IDENTIFIER.matches(identifier).value(identifier));
        if (patientId != null && patientId.trim().length() > 0)
            paramlist.add(ImagingObjectSelection.PATIENT.hasId(patientId));
        if (selectedStudy != null && selectedStudy.trim().length() > 0)
            paramlist.add(ImagingObjectSelection.SELECTED_STUDY.matches(selectedStudy).value(selectedStudy));
        if (title != null && title.trim().length() > 0)
            paramlist.add(ImagingObjectSelection.TITLE.exactly().code(title));
        if (authoringStartDate != null)
            paramlist.add(ImagingObjectSelection.AUTHORING_TIME.afterOrEquals().day(authoringStartDate));
        if (authoringEndDate != null)
            paramlist.add(ImagingObjectSelection.AUTHORING_TIME.beforeOrEquals().day(authoringEndDate));
        return searchResourceByParamsAll(ImagingObjectSelection.class, paramlist);
    }

    public String createMedicationOrder(MedicationOrder prescription) {
        return createResource(prescription);
    }

    public MedicationOrder readMedicationOrder(String id) {
        return (MedicationOrder) readResource(MedicationOrder.class, id);
    }

    public String updateMedicationOrder(String targetId, MedicationOrder updatePrescription) {
        return updateResource(MedicationOrder.class, targetId, updatePrescription);
    }

    public IIdType updateMedicationOrderReturnId(String targetId, MedicationOrder updatePrescription) {
        return updateResourceReturnId(MedicationOrder.class, targetId, updatePrescription);
    }

    public List<MedicationOrder> searchMedicationOrderById(String id) {
        return searchResource(MedicationOrder.class, MedicationOrder.RES_ID.matchesExactly().value(id));
    }

    public List<MedicationOrder> searchMedicationOrderByIdentifier(String identifier) {
        return searchResource(MedicationOrder.class, MedicationOrder.IDENTIFIER.exactly().code(identifier));
    }

    public List<MedicationOrder> searchMedicationOrderByParams(String subjectId,
            MedicationOrderStatusEnum status, Date prescriptionCreateStartDate, Date prescriptionCreateEndDate) {
        List<ICriterion> paramlist = new ArrayList<ICriterion>();
        if (subjectId != null && subjectId.trim().length() > 0)
            paramlist.add(MedicationOrder.PATIENT.hasId(subjectId));
        if (status != null && status.getCode().trim().length() > 0)
            paramlist.add(MedicationOrder.STATUS.exactly().code(status.getCode()));
        if (prescriptionCreateStartDate != null)
            paramlist.add(MedicationOrder.DATEWRITTEN.afterOrEquals().day(prescriptionCreateStartDate));
        if (prescriptionCreateEndDate != null)
            paramlist.add(MedicationOrder.DATEWRITTEN.beforeOrEquals().day(prescriptionCreateEndDate));
        return searchResourceByParamsAll(MedicationOrder.class, paramlist);
    }

    public String createCondition(Condition condition) {
        return createResource(condition);
    }

    public Condition readCondition(String id) {
        return (Condition) readResource(Condition.class, id);
    }

    public String updateCondition(String targetId, Condition updateCondition) {
        return updateResource(Condition.class, targetId, updateCondition);
    }

    public IIdType updateConditionReturnId(String targetId, Condition updateCondition) {
        return updateResourceReturnId(Condition.class, targetId, updateCondition);
    }

    public List<Condition> searchConditionById(String id) {
        return searchResource(Condition.class, Condition.RES_ID.matchesExactly().value(id));
    }

    public List<Condition> searchConditionByParams(String patientId, String asserterId, ConditionClinicalStatusCodesEnum status,
            String categoryCode, String conditionCode, String encounterId, Date conditionCreateStartDate,
            Date conditionCreateEndDate) {
        List<ICriterion> paramlist = new ArrayList<ICriterion>();
        if (patientId != null && patientId.trim().length() > 0)
            paramlist.add(Condition.PATIENT.hasId(patientId));
        if (asserterId != null && asserterId.trim().length() > 0)
            paramlist.add(Condition.ASSERTER.hasId(asserterId));
        if (status != null && status.getCode().trim().length() > 0)
            paramlist.add(Condition.CLINICALSTATUS.exactly().code(status.getCode()));
        if (categoryCode != null && categoryCode.trim().length() > 0)
            paramlist.add(Condition.CATEGORY.exactly().code(categoryCode));
        if (conditionCode != null && conditionCode.trim().length() > 0)
            paramlist.add(Condition.CODE.exactly().code(conditionCode));
        if (encounterId != null && encounterId.trim().length() > 0)
            paramlist.add(Condition.ENCOUNTER.hasId(encounterId));
        if (conditionCreateStartDate != null)
            paramlist.add(Condition.DATE_RECORDED.afterOrEquals().day(conditionCreateStartDate));
        if (conditionCreateEndDate != null)
            paramlist.add(Condition.DATE_RECORDED.beforeOrEquals().day(conditionCreateEndDate));
        return searchResourceByParamsAll(Condition.class, paramlist);
    }

    public String createObservation(Observation observation) {
        return createResource(observation);
    }

    public Observation readObservation(String id) {
        return (Observation) readResource(Observation.class, id);
    }

    public String updateObservation(String targetId, Observation updateObservation) {
        return updateResource(Observation.class, targetId, updateObservation);
    }

    public IIdType updateObservationReturnId(String targetId, Observation updateObservation) {
        return updateResourceReturnId(Observation.class, targetId, updateObservation);
    }

    public List<Observation> searchObservationById(String id) {
        return searchResource(Observation.class, Observation.RES_ID.matchesExactly().value(id));
    }

    public List<Observation> searchObservationByParams(String subjectId, String performerId, ObservationStatusEnum status,
            String observationCode, Date observationStartDate, Date observationEndDate) {
        List<ICriterion> paramlist = new ArrayList<ICriterion>();
        if (subjectId != null && subjectId.trim().length() > 0)
            paramlist.add(Observation.SUBJECT.hasId(subjectId));
        if (performerId != null && performerId.trim().length() > 0)
            paramlist.add(Observation.PERFORMER.hasId(performerId));
        if (status != null && status.getCode().trim().length() > 0)
            paramlist.add(Observation.STATUS.exactly().code(status.getCode()));
        if (observationCode != null && observationCode.trim().length() > 0)
            paramlist.add(Observation.CODE.exactly().code(observationCode));
        if (observationStartDate != null)
            paramlist.add(Observation.DATE.afterOrEquals().day(observationStartDate));
        if (observationEndDate != null)
            paramlist.add(Observation.DATE.beforeOrEquals().day(observationEndDate));
        return searchResourceByParamsAll(Observation.class, paramlist);
    }

    public String createProcedure(Procedure observation) {
        return createResource(observation);
    }

    public Procedure readProcedure(String id) {
        return (Procedure) readResource(Procedure.class, id);
    }

    public String updateProcedure(String targetId, Procedure updateProcedure) {
        return updateResource(Procedure.class, targetId, updateProcedure);
    }

    public IIdType updateProcedureReturnId(String targetId, Procedure updateProcedure) {
        return updateResourceReturnId(Procedure.class, targetId, updateProcedure);
    }

    public List<Procedure> searchProcedureById(String id) {
        return searchResource(Procedure.class, Procedure.RES_ID.matchesExactly().value(id));
    }

    public List<Procedure> searchProcedureByParams(String patientId, String performerId, String observationCode,
            Date procedureStartDate, Date procedureEndDate) {
        List<ICriterion> paramlist = new ArrayList<ICriterion>();
        if (patientId != null && patientId.trim().length() > 0)
            paramlist.add(Procedure.PATIENT.hasId(patientId));
        if (observationCode != null && observationCode.trim().length() > 0)
            paramlist.add(Procedure.CODE.exactly().code(observationCode));
        if (procedureStartDate != null)
            paramlist.add(Procedure.DATE.afterOrEquals().day(procedureStartDate));
        if (procedureEndDate != null)
            paramlist.add(Procedure.DATE.beforeOrEquals().day(procedureEndDate));
        return searchResourceByParamsAll(Procedure.class, paramlist);
    }

    public String createEncounter(Encounter encounter) {
        return createResource(encounter);
    }

    public Encounter readEncounter(String id) {
        return (Encounter) readResource(Encounter.class, id);
    }

    public String updateEncounter(String targetId, Encounter updateEncounter) {
        return updateResource(Encounter.class, targetId, updateEncounter);
    }

    public IIdType updateEncounterReturnId(String targetId, Encounter updateEncounter) {
        return updateResourceReturnId(Encounter.class, targetId, updateEncounter);
    }

    public List<Encounter> searchEncounternById(String id) {
        return searchResource(Encounter.class, Encounter.RES_ID.matchesExactly().value(id));
    }

    public List<Encounter> searchEncounterByIdentifier(String identifier) {
        return searchResource(Encounter.class, Encounter.IDENTIFIER.exactly().code(identifier));
    }

    public List<Encounter> searchEncounterByParams(String patientId, EncounterStateEnum status, Date encounterStartDate,
            Date encounterCreateEndDate) {
        List<ICriterion> paramlist = new ArrayList<ICriterion>();
        if (patientId != null && patientId.trim().length() > 0)
            paramlist.add(Encounter.PATIENT.hasId(patientId));
        if (status != null && status.getCode().trim().length() > 0)
            paramlist.add(Encounter.STATUS.exactly().code(status.getCode()));
        if (encounterStartDate != null)
            paramlist.add(Encounter.DATE.afterOrEquals().day(encounterStartDate));
        if (encounterCreateEndDate != null)
            paramlist.add(Encounter.DATE.beforeOrEquals().day(encounterCreateEndDate));
        return searchResourceByParamsAll(Encounter.class, paramlist);
    }

    public Patient getPatient(String id, String name, AdministrativeGenderEnum sex, Date dob) {
        Patient recordTarget = null;
        List<Patient> patientResult = searchPatientById(id);
        if (patientResult != null && !patientResult.isEmpty()) {
            recordTarget = patientResult.get(0);
        } else {
            IIdType pid = createResourceReturnId(createPatient(id, name, sex, dob));
            if (pid != null)
                recordTarget = readPatient(pid.getIdPart());

        }
        System.out.println("Patient done");
        return recordTarget;
    }

    public Practitioner getPractitioner(String id, String localId, String localIdSystem, String name,
            AdministrativeGenderEnum sex, Date dob, String tel, String email, String organizationRef,
            PractitionerRoleEnum pr, String specialtyCode, String specialtySystem, String specialtyName) {
        Practitioner author = null;
        List<Practitioner> practitionerResult = searchPractitionerById(id);
        if (practitionerResult != null && !practitionerResult.isEmpty()) {
            author = practitionerResult.get(0);
        } else {
            IIdType aid = createResourceReturnId(createPractitioner(id, localId, localIdSystem, name, sex, new DateDt(dob),
                    tel, email, organizationRef, pr, specialtyCode, specialtySystem, specialtyName));
            if (aid != null)
                author = readPractitioner(aid.getIdPart());
        }
        System.out.println("Practitioner done");
        return author;
    }

    public String uploadEMR(byte[] file) {
        if (file != null && file.length > 0) {
            Patient recordTarget = null;
            Practitioner author = null;
            // from CDA

            String recordTargetId = "A123456789";
            String pName = "";
            AdministrativeGenderEnum pSex = AdministrativeGenderEnum.MALE;
            Date pDob = new Date();

            String authorId = "A123456789";
            String custodianId = "1.2.840.269649.231";
            String fileName = "";
            String classCode = "";
            String className = "";
            String typeCode = "";
            String typeName = "";

            try {
                // get Patient Info from CDA
                getPatient(recordTargetId, pName, pSex, pDob);

                // get Author Info from CDA
                List<Practitioner> practitionerResult = searchPractitionerById(authorId);
                // System.out.println("Patient search result:" + practitionerResult.size());
                if (practitionerResult.size() < 1) {
                    // TODO
                    String aid = createResource(createPractitioner(null, null, null, null, null, null, null, null, null, null,
                            null, null, null));
                    if (aid != null)
                        author = (Practitioner) readResource(Practitioner.class, aid);
                } else
                    author = practitionerResult.get(0);
                System.out.println("Practitioner done");

                return uploadEMR(recordTarget, author, custodianId, fileName, file, classCode, className, typeCode, typeName);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public String uploadEMR(Patient recordTarget, Practitioner author, String custodianId, String fileName, byte[] file,
            String classCode, String className, String typeCode, String typeName) {
        String transactionResult = null;
        if (recordTarget != null && author != null && custodianId != null && file != null) {
            IIdType bid = createResourceReturnId(createBinary(null, "text/xml", file, null));
            System.out.println("Binary done");
            if (bid != null) {
                // DocumentReference docRef = createUploadDocumentReference(recordTarget, author, custodianId,
                // getReferenceId(bid), file);
                DocumentReference docRef = createEMRDocumentReference(recordTarget, author, custodianId, fileName,
                        getReferenceId(bid), file, null, null, classCode, className, typeCode, typeName);
                IIdType did = createResourceReturnId(docRef);
                System.out.println("DocumentReference done");
                if (did != null) {
                    transactionResult = did.getIdPart();
                    AuditEvent auditLog = createAuditEvent(null, author.getId().getValue(), author.getName()
                            .getNameAsSingleString(), author.addPractitionerRole().getRole().getCodingFirstRep().getCode(), 
                            author.addPractitionerRole().getRole().getCodingFirstRep().getSystem(), 
                            author.addPractitionerRole().getRole().getCodingFirstRep().getDisplay(), connectUrl, 
                            getReferenceId(did), null, AuditEventActionEnum.CREATE, AuditEventOutcomeEnum.SUCCESS,
                            AuditEventOutcomeEnum.SUCCESS.name(), new Date());
                    String aid = createResource(auditLog);
                    if (aid != null)
                        System.out.println("Upload Success done");
                } else {
                    AuditEvent auditLog = createAuditEvent(null, author.getId().getValue(), author.getName()
                            .getNameAsSingleString(), author.addPractitionerRole().getRole().getCodingFirstRep().getCode(), 
                            author.addPractitionerRole().getRole().getCodingFirstRep().getSystem(), 
                            author.addPractitionerRole().getRole().getCodingFirstRep().getDisplay(), connectUrl, 
                            getReferenceId(recordTarget.getId()), null, AuditEventActionEnum.CREATE, AuditEventOutcomeEnum.MINOR_FAILURE,
                            AuditEventOutcomeEnum.MINOR_FAILURE.name(), new Date());
                    String aid = createResource(auditLog);
                    if (aid != null)
                        System.out.println("Upload Fail done");
                }
            }
        }
        return transactionResult;
    }

    public String UpdateEMR(Practitioner author, String custodianId, String targetDocId, String fileName, byte[] file) {
        String transactionResult = null;
        if (author != null && custodianId != null && targetDocId != null && file != null) {
            try {
                List<DocumentReference> docRefResult = searchDocumentReferenceById(targetDocId);
                if (docRefResult != null && docRefResult.size() > 0) {
                    DocumentReference existDocRef = docRefResult.get(0);
                    IIdType bid = createResourceReturnId(createBinary(null, "text/xml", file, null));
                    System.out.println("Create Binary done");
                    if (bid != null) {
                        // DocumentReference updateDocRef = createUploadDocumentReference(recordTarget, author,
                        // custodianId, getReferenceId(bid), file);
                        // updateDocRef.setId(existDocRef.getId());
                        existDocRef.setDescription(fileName);
                        //existDocRef.setHash(getSha1(file));
                        //existDocRef.setService(new Service().setAddress(getReferenceId(bid)));
                        IIdType did = updateDocumentReferenceReturnId(targetDocId, existDocRef);
                        System.out.println("Update DocumentReference done");
                        if (did != null) {
                            transactionResult = did.getIdPart();
                            AuditEvent auditLog = createAuditEvent(null, author.getId().getValue(), author.getName()
                                    .getNameAsSingleString(), author.addPractitionerRole().getRole().getCodingFirstRep().getCode(), 
                                    author.addPractitionerRole().getRole().getCodingFirstRep().getSystem(), 
                                    author.addPractitionerRole().getRole().getCodingFirstRep().getDisplay(), connectUrl,
                                    getReferenceId(did), null, AuditEventActionEnum.CREATE,
                                    AuditEventOutcomeEnum.SUCCESS, AuditEventOutcomeEnum.SUCCESS.name(), new Date());
                            String aid = createResource(auditLog);
                            if (aid != null)
                                System.out.println("Update Success done");
                        } else {
                            AuditEvent auditLog = createAuditEvent(null, author.getId().getValue(), author.getName()
                                    .getNameAsSingleString(), author.addPractitionerRole().getRole().getCodingFirstRep().getCode(), 
                                    author.addPractitionerRole().getRole().getCodingFirstRep().getSystem(), 
                                    author.addPractitionerRole().getRole().getCodingFirstRep().getDisplay(), connectUrl,
                                    existDocRef.getId().getValue(), null, AuditEventActionEnum.CREATE,
                                    AuditEventOutcomeEnum.MINOR_FAILURE, AuditEventOutcomeEnum.MINOR_FAILURE.name(),
                                    new Date());
                            String aid = createResource(auditLog);
                            if (aid != null)
                                System.out.println("Update Fail done");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return transactionResult;
    }

    public List<DocumentReference> QueryEMR(String existedPid, Date serviceStartDate, Date serviceEndDate) {
        Patient targetPatient = null;
        List<DocumentReference> docList = null;
        try {
            List<Patient> patientResult = searchPatientById(existedPid);
            if (patientResult != null && !patientResult.isEmpty()) {
                targetPatient = patientResult.get(0);
                docList = searchDocumentReferenceByParams(null, null, null, null, null, null, null, null, null, null, null,
                        getReferenceId(targetPatient.getId()), null, serviceStartDate, serviceEndDate);
                if (docList != null && !docList.isEmpty()) {
                    System.out.println("DocumentReference result:" + docList.size());
                    List<IIdType> docIdList = new ArrayList<IIdType>();
                    for (DocumentReference doc : docList) {
                        docIdList.add(doc.getId());
                    }
                    AuditEvent auditLog = createAuditEvent(null, targetPatient.getId().getValue(), targetPatient
                            .getName().toString(), null, null, null, connectUrl, null, docIdList,
                            AuditEventActionEnum.CREATE, AuditEventOutcomeEnum.SUCCESS,
                            AuditEventOutcomeEnum.SUCCESS.name(), new Date());
                    String aid = createResource(auditLog);
                    if (aid != null)
                        System.out.println("Query Success done");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return docList;
    }

    public Binary RetrieveEMR(String binaryId) {
        Binary binary = null;
        try {
            List<Binary> binaryResult = searchBinaryById(binaryId);
            if (binaryResult != null && !binaryResult.isEmpty()) {
                // System.out.println("Binary read result:" + binary.getContentAsBase64());
                binary = binaryResult.get(0);
            }

            AuditEvent auditLog = createAuditEvent(null, getReferenceId(binary.getId()), binary.getContentType(), null,
                    null, null, connectUrl, getReferenceId(binary.getId()), null, AuditEventActionEnum.CREATE,
                    AuditEventOutcomeEnum.SUCCESS, AuditEventOutcomeEnum.SUCCESS.name(), new Date());
            String aid = createResource(auditLog);
            if (aid != null)
                System.out.println("Retrieve Success done");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return binary;
    }

    public String uploadMedicationList(String prescriptionId, String orgId, Patient patient, Practitioner author,
            List<MedicationOrder> prescriptionList) {
        String transactionResult = null;
        if (patient != null && author != null && prescriptionList != null) {
            List<IIdType> prescriptionRefList = new ArrayList<IIdType>();
            List<MedicationOrder> createdList = new ArrayList<MedicationOrder>();
            if (prescriptionList.size() > 0) {
                for (MedicationOrder prescription : prescriptionList)
                    prescriptionRefList.add(createResourceReturnId(prescription));
                if (prescriptionRefList.size() > 0) {
                    for (IIdType createdPrescriptionRef : prescriptionRefList)
                        createdList.add(readMedicationOrder(createdPrescriptionRef.getIdPart()));
                }
            }
            System.out.println("MedicationOrderList done");

            IIdType lid = createResourceReturnId(createMedicationList(prescriptionId, orgId, patient, author,
                    ListModeEnum.WORKING_LIST, createdList));
            System.out.println("List done");
            if (lid != null) {
                transactionResult = lid.getIdPart();
                AuditEvent auditLog = createAuditEvent(null, author.getId().getValue(), author.getName()
                        .getNameAsSingleString(), author.addPractitionerRole().getRole().getCodingFirstRep().getCode(), 
                        author.addPractitionerRole().getRole().getCodingFirstRep().getSystem(), 
                        author.addPractitionerRole().getRole().getCodingFirstRep().getDisplay(), connectUrl, getReferenceId(lid), prescriptionRefList,
                        AuditEventActionEnum.CREATE, AuditEventOutcomeEnum.SUCCESS,
                        AuditEventOutcomeEnum.SUCCESS.name(), new Date());
                String aid = createResource(auditLog);
                if (aid != null)
                    System.out.println("Upload MedicationList Success done");
            } else {
                AuditEvent auditLog = createAuditEvent(null, author.getId().getValue(), author.getName()
                        .getNameAsSingleString(), author.addPractitionerRole().getRole().getCodingFirstRep().getCode(), 
                        author.addPractitionerRole().getRole().getCodingFirstRep().getSystem(), 
                        author.addPractitionerRole().getRole().getCodingFirstRep().getDisplay(), connectUrl, null, prescriptionRefList,
                        AuditEventActionEnum.CREATE, AuditEventOutcomeEnum.MINOR_FAILURE,
                        AuditEventOutcomeEnum.MINOR_FAILURE.name(), new Date());
                String aid = createResource(auditLog);
                if (aid != null)
                    System.out.println("Upload MedicationList Fail done");
            }
        }
        return transactionResult;
    }

    public String uploadDiagnosisList(String disgnosisId, String orgId, Patient patient, Practitioner author,
            List<Condition> diagnosisList) {
        String transactionResult = null;
        if (patient != null && author != null && diagnosisList != null) {
            List<IIdType> diagnosisRefList = new ArrayList<IIdType>();
            List<Condition> createdList = new ArrayList<Condition>();
            if (diagnosisList.size() > 0) {
                for (Condition diagnosis : diagnosisList)
                    diagnosisRefList.add(createResourceReturnId(diagnosis));
                if (diagnosisRefList.size() > 0) {
                    for (IIdType createdDiagnosisRef : diagnosisRefList)
                        createdList.add(readCondition(createdDiagnosisRef.getIdPart()));
                }
            }
            System.out.println("DiagnosisList done");

            IIdType lid = createResourceReturnId(createDiagnosisList(disgnosisId, orgId, patient, author, ListModeEnum.WORKING_LIST,
                    createdList));
            System.out.println("List done");
            if (lid != null) {
                transactionResult = lid.getIdPart();
                AuditEvent auditLog = createAuditEvent(null, author.getId().getValue(), author.getName()
                        .getNameAsSingleString(), author.addPractitionerRole().getRole().getCodingFirstRep().getCode(), 
                        author.addPractitionerRole().getRole().getCodingFirstRep().getSystem(), 
                        author.addPractitionerRole().getRole().getCodingFirstRep().getDisplay(), connectUrl, getReferenceId(lid), diagnosisRefList,
                        AuditEventActionEnum.CREATE, AuditEventOutcomeEnum.SUCCESS,
                        AuditEventOutcomeEnum.SUCCESS.name(), new Date());
                String aid = createResource(auditLog);
                if (aid != null)
                    System.out.println("Upload DiagnosisList Success done");
            } else {
                AuditEvent auditLog = createAuditEvent(null, author.getId().getValue(), author.getName()
                        .getNameAsSingleString(), author.addPractitionerRole().getRole().getCodingFirstRep().getCode(), 
                        author.addPractitionerRole().getRole().getCodingFirstRep().getSystem(), 
                        author.addPractitionerRole().getRole().getCodingFirstRep().getDisplay(), connectUrl, null, diagnosisRefList,
                        AuditEventActionEnum.CREATE, AuditEventOutcomeEnum.MINOR_FAILURE,
                        AuditEventOutcomeEnum.MINOR_FAILURE.name(), new Date());
                String aid = createResource(auditLog);
                if (aid != null)
                    System.out.println("Upload DiagnosisList Fail done");
            }
        }
        return transactionResult;
    }

    public String uploadObservationList(String observationId, String orgId, Patient patient, Practitioner author,
            List<Observation> observationList) {
        String transactionResult = null;
        if (patient != null && author != null && observationList != null) {
            List<IIdType> observationRefList = new ArrayList<IIdType>();
            List<Observation> createdList = new ArrayList<Observation>();
            if (observationList.size() > 0) {
                for (Observation observation : observationList)
                    observationRefList.add(createResourceReturnId(observation));
                if (observationRefList.size() > 0) {
                    for (IIdType createdObservationRef : observationRefList)
                        createdList.add(readObservation(createdObservationRef.getIdPart()));
                }
            }
            System.out.println("ObservationList done");

            IIdType lid = createResourceReturnId(createObservationList(observationId, orgId, patient, author,
                    ListModeEnum.WORKING_LIST, createdList));
            System.out.println("List done");
            if (lid != null) {
                transactionResult = lid.getIdPart();
                AuditEvent auditLog = createAuditEvent(null, author.getId().getValue(), author.getName()
                        .getNameAsSingleString(), author.addPractitionerRole().getRole().getCodingFirstRep().getCode(), 
                        author.addPractitionerRole().getRole().getCodingFirstRep().getSystem(), 
                        author.addPractitionerRole().getRole().getCodingFirstRep().getDisplay(), connectUrl, getReferenceId(lid), observationRefList,
                        AuditEventActionEnum.CREATE, AuditEventOutcomeEnum.SUCCESS,
                        AuditEventOutcomeEnum.SUCCESS.name(), new Date());
                String aid = createResource(auditLog);
                if (aid != null)
                    System.out.println("Upload ObservationList Success done");
            } else {
                AuditEvent auditLog = createAuditEvent(null, author.getId().getValue(), author.getName()
                        .getNameAsSingleString(), author.addPractitionerRole().getRole().getCodingFirstRep().getCode(), 
                        author.addPractitionerRole().getRole().getCodingFirstRep().getSystem(), 
                        author.addPractitionerRole().getRole().getCodingFirstRep().getDisplay(), connectUrl, null, observationRefList,
                        AuditEventActionEnum.CREATE, AuditEventOutcomeEnum.MINOR_FAILURE,
                        AuditEventOutcomeEnum.MINOR_FAILURE.name(), new Date());
                String aid = createResource(auditLog);
                if (aid != null)
                    System.out.println("Upload ObservationList Fail done");
            }
        }
        return transactionResult;
    }

    public String uploadProcedureList(String procedureId, String orgId, Patient patient, Practitioner author,
            List<Procedure> procedureList) {
        String transactionResult = null;
        if (patient != null && author != null && procedureList != null) {
            List<IIdType> procedureRefList = new ArrayList<IIdType>();
            List<Procedure> createdList = new ArrayList<Procedure>();
            if (procedureList.size() > 0) {
                for (Procedure procedure : procedureList)
                    procedureRefList.add(createResourceReturnId(procedure));
                if (procedureRefList.size() > 0) {
                    for (IIdType createdProcedureRef : procedureRefList)
                        createdList.add(readProcedure(createdProcedureRef.getIdPart()));
                }
            }
            System.out.println("ProcedureList done");

            IIdType lid = createResourceReturnId(createProcedureList(procedureId, orgId, patient, author, ListModeEnum.WORKING_LIST,
                    createdList));
            System.out.println("List done");
            if (lid != null) {
                transactionResult = lid.getIdPart();
                AuditEvent auditLog = createAuditEvent(null, author.getId().getValue(), author.getName()
                        .getNameAsSingleString(), author.addPractitionerRole().getRole().getCodingFirstRep().getCode(), 
                        author.addPractitionerRole().getRole().getCodingFirstRep().getSystem(), 
                        author.addPractitionerRole().getRole().getCodingFirstRep().getDisplay(), connectUrl, getReferenceId(lid), procedureRefList,
                        AuditEventActionEnum.CREATE, AuditEventOutcomeEnum.SUCCESS,
                        AuditEventOutcomeEnum.SUCCESS.name(), new Date());
                String aid = createResource(auditLog);
                if (aid != null)
                    System.out.println("Upload ProcedureList Success done");
            } else {
                AuditEvent auditLog = createAuditEvent(null, author.getId().getValue(), author.getName()
                        .getNameAsSingleString(), author.addPractitionerRole().getRole().getCodingFirstRep().getCode(), 
                        author.addPractitionerRole().getRole().getCodingFirstRep().getSystem(), 
                        author.addPractitionerRole().getRole().getCodingFirstRep().getDisplay(), connectUrl, null, procedureRefList,
                        AuditEventActionEnum.CREATE, AuditEventOutcomeEnum.MINOR_FAILURE,
                        AuditEventOutcomeEnum.MINOR_FAILURE.name(), new Date());
                String aid = createResource(auditLog);
                if (aid != null)
                    System.out.println("Upload ProcedureList Fail done");
            }
        }
        return transactionResult;
    }

    public String getReferenceId(IIdType id) {
        return id.getResourceType() + "/" + id.getIdPart();
    }

    public String getReferenceIdWithVersion(IdDt id) {
        return id.getResourceType() + "/" + id.getVersionIdPart();
    }

    public Bundle setFhirBase(Bundle bundle) {
        // TODO reassign bundle LinkBase/LinkNext/LinkPrevious
        Bundle reviseBundle = null;
        if (!bundle.getLinkBase().isEmpty()) {
            String returnBase = bundle.getLinkBase().getValue();
            if (!bundle.getLinkNext().isEmpty()) {
                String returnLinkNext = bundle.getLinkNext().getValue();
                // bundle.setLinkNext(returnLinkNext.replaceFirst(returnBase, connectUrl));
            }
            if (!bundle.getLinkPrevious().isEmpty()) {
                String returnLinkPrevious = bundle.getLinkPrevious().getValue();
                // bundle.setLinkPrevious(returnLinkPrevious.replaceFirst(returnBase, connectUrl));
            }
            // bundle.setLinkBase(connectUrl);
            reviseBundle = bundle;
        }
        return reviseBundle;
    }

    private Organization createDTCOrganization() {
        return createOrganization("1531000001", "1.2.840.269649", "測試醫院", "28094611", "test@test.com");
        //return createOrganization("1531000231", "H1531000231", "合華醫院", "0912345678", "test@test.com");
    }

    private Organization createOrganization(String id, String oid, String name, String tel, String email) {
        return createOrganization(id, oid, "2.16.886.101.20003.20014.20006", name, tel, null, null, null, null, null, email,
                null, null, OrganizationTypeEnum.HOSPITAL_DEPARTMENT);
    }

    public Organization createOrganization(String id, String oid, String idSystem, String name, String tel, String addZip,
            String addCountry, String addState, String addCity, String addLine, String email, String contactGivenName,
            String contactFamilyName, OrganizationTypeEnum orgType) {
        Organization org = new Organization();
        //org.setId(new IdDt(oid));
        //org.setId(oid);
        org.setName(name).addIdentifier().setSystem(idSystem).setValue(id);
        org.addTelecom().setValue(tel);
        org.addAddress().setPostalCode(addZip).setCountry(addCountry).setState(addState).setCity(addCity).addLine(addLine);
        org.addContact().setName(new HumanNameDt().addGiven(contactGivenName).addFamily(contactFamilyName))
                .setAddress(new AddressDt().addLine(email));
        org.setType(orgType);
        // org.setText(null);

        IParser p = new FhirContext().forDstu2().newJsonParser().setPrettyPrint(true);
        String messageString = p.encodeResourceToString(org);
        System.out.println(messageString);
        return org;
    }
    
    public Organization createOrganization(String id, String identifier, String identifierSystem, String name, String tel, String addZip,
            String addCountry, String addState, String addCity, String addDistrict, String addLine, String email, String contactGivenName,
            String contactFamilyName, String[] orgType, String orgTypeSystem, String partOfOrgId, String partOfOrgName) {
        Organization org = new Organization();
        org.setId(new IdDt(id));
        org.setName(name).addIdentifier().setSystem(identifierSystem).setValue(identifier);
        org.addTelecom().setValue(tel);
        org.addAddress().setPostalCode(addZip).setCountry(addCountry).setState(addState).setCity(addCity).setDistrict(addDistrict).addLine(addLine);
        org.addContact().setName(new HumanNameDt().addGiven(contactGivenName).addFamily(contactFamilyName))
                .setAddress(new AddressDt().addLine(email));
        BoundCodeableConceptDt<OrganizationTypeEnum> bccd = org.getType();
        if (orgType != null && orgType.length > 0) {
            for (String type : orgType) {
                bccd.addCoding().setCode(type).setSystem(orgTypeSystem);
            }
        }
        org.setType(bccd);
        org.setPartOf(new ResourceReferenceDt(new IdDt(partOfOrgId)).setDisplay(partOfOrgName));
        // org.setText(null);

        IParser p = new FhirContext().forDstu2().newJsonParser().setPrettyPrint(true);
        String messageString = p.encodeResourceToString(org);
        System.out.println(messageString);
        return org;
    }

    private Practitioner createDTCPractitioner() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -20);
        return createPractitioner("BA00243739", "3739", "2.16.886.101.20003.20060", "测试医师3739", AdministrativeGenderEnum.MALE,
                new DateDt(cal.getTime()), "0912345678", "testDr@test.com", "Organization/3",
                PractitionerRoleEnum.DOCTOR, null, null, null);
    }

    public Practitioner createPractitioner(String id, String localId, String localIdSystem, String name,
            AdministrativeGenderEnum sex, DateDt dob, String tel, String email, String organizationRef,
            PractitionerRoleEnum pr, String specialtyCode, String specialtySystem, String specialtyName) {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(id);
        if (localId != null && localId.trim().length() > 0)
            practitioner.addIdentifier().setSystem(localIdSystem).setValue(localId);
        practitioner.setName(new HumanNameDt().addGiven(name).setText(name));
        practitioner.setGender(sex).setBirthDate(dob);
        practitioner.addTelecom().setValue(tel);
        practitioner.addAddress(new AddressDt().setText(email));
        practitioner.addPractitionerRole().setManagingOrganization(new ResourceReferenceDt(organizationRef)).setRole(pr)
                .addSpecialty().addCoding().setCode(specialtyCode).setSystem(specialtySystem).setDisplay(specialtyName);
        // practitioner.addLocation();
        // practitioner.addPhoto();
        // practitioner.addQualification();

        IParser p = new FhirContext().forDstu2().newJsonParser().setPrettyPrint(true);
        String messageString = p.encodeResourceToString(practitioner);
        System.out.println(messageString);
        return practitioner;
    }

    /*
     * private AuditEvent createTestAuditEvent() { return createAuditEvent(null, "1531000001", "測試醫院", null,
     * null, null, null, null, AuditEventActionEnum.CREATE, AuditEventOutcomeEnum.SUCCESS, null, null); }
     */

    public AuditEvent createAuditEvent(String id, String participantId, String participantName, String roleCode,
            String roleSystem, String roleDisplay, String sourceRef, String resourceRef, List<IIdType> resourceRefList,
            AuditEventActionEnum actionEnum, AuditEventOutcomeEnum outcomeEnum, String outcomeDesc, Date eventDate) {
        AuditEvent event = new AuditEvent();
        if (id == null)
            id = String.valueOf(System.nanoTime());
        event.setId("sid:" + id);
        Event e = new Event();
        if (eventDate == null)
            eventDate = new Date();
        e.setAction(actionEnum).setOutcome(outcomeEnum).setDateTime(new InstantDt(eventDate)).setOutcomeDesc(outcomeDesc);
        // local Type define
        // CodeableConceptDt exampleCode = new CodeableConceptDt("http://www.nhi.gov.tw/", "A000015421");
        // exampleCode.setText("YEN KUANG EYE DROPS");
        // e.setType(exampleCode);

        Source s = new Source();
        s.setSite(sourceRef);
        // s.setIdentifier(sourceRef).setSite(sourceRef).addType().setCode(sourceRef).setSystem(sourceRef).setDisplay(sourceRef);

        event.setEvent(e).setSource(s);
        List<ObjectElement> ObjectList = new ArrayList<ObjectElement>();
        if (resourceRef != null)
            event.addObject().setReference(new ResourceReferenceDt(resourceRef));
        if (resourceRefList != null && resourceRefList.size() > 0) {
            for (IIdType ref : resourceRefList) {
                ObjectList.add(new ObjectElement().setReference(new ResourceReferenceDt(getReferenceId(ref))));
            }
            event.setObject(ObjectList);
        }
        event.addParticipant().setUserId(new IdentifierDt().setValue(participantId)).setName(participantName).addRole().addCoding().setCode(roleCode)
                .setSystem(roleSystem).setDisplay(roleDisplay);
        event.setText(null);

        IParser p = new FhirContext().forDstu2().newJsonParser().setPrettyPrint(true);
        String messageString = p.encodeResourceToString(event);
        System.out.println(messageString);
        return event;
    }

    private Patient createTestPatient() throws Exception {
        Calendar cal = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("ddHHmmss");
        String datestring = dateFormat.format(cal.getTime());
        cal.add(Calendar.YEAR, -20);
        return createPatient("Z1" + datestring, "測試人", AdministrativeGenderEnum.MALE, cal.getTime());
    }

    public Patient createPatient(String id, String name, AdministrativeGenderEnum sex, Date dob) {
        return createPatient(id, "2.16.886.101.20003.20001", name, sex, new DateDt(dob), null, null, null, null, null,
                null, null, null);
    }

    private Patient createPatient(String id, String idSystem, String name, AdministrativeGenderEnum sex, DateDt dob,
            MaritalStatusCodesEnum marryStatus, String tel, String addZip, String addCountry, String addState, String addCity,
            String addLine, String email) {
        Patient patient = new Patient();
        //patient.setId(id);
        patient.addIdentifier().setSystem(idSystem).setValue(id);
        patient.addName().addGiven(name).setText(name);
        patient.setGender(sex).setBirthDate(dob).setMaritalStatus(marryStatus);
        patient.addTelecom().setValue(tel);
        patient.addAddress().setPostalCode(addZip).setCountry(addCountry).setState(addState).setCity(addCity).addLine(addLine);
        patient.addLink().setElementSpecificId(email);
        // patient.setManagingOrganization(null);

        // patient.addContact();
        // patient.addCareProvider();
        // patient.addPhoto();
        // patient.setText(null);

        System.out.println(patient.getId().getIdPart());
        IParser p = new FhirContext().forDstu2().newJsonParser().setPrettyPrint(true);
        String messageString = p.encodeResourceToString(patient);
        System.out.println(messageString);
        return patient;
    }
    
    public Patient createPatient(String id, String identifier, String identifierSystem, String managingOrgName, String name, AdministrativeGenderEnum sex, DateDt dob,
            MaritalStatusCodesEnum marryStatus, String tel, String addZip, String addCountry, String addState, String addCity,
            String addDistrict, String addLine, String email) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.addIdentifier().setSystem(identifierSystem).setValue(identifier);
        patient.addName().setText(name);
        patient.setGender(sex).setBirthDate(dob).setMaritalStatus(marryStatus);
        patient.addTelecom().setValue(tel);
        patient.addAddress().setPostalCode(addZip).setCountry(addCountry).setState(addState).setCity(addCity).setDistrict(addDistrict).addLine(addLine);
        patient.addLink().setElementSpecificId(email);
        patient.setManagingOrganization(new ResourceReferenceDt().setReference(identifierSystem).setDisplay(managingOrgName));
        // patient.setManagingOrganization(null);

        // patient.addContact();
        // patient.addCareProvider();
        // patient.addPhoto();
        // patient.setText(null);

        System.out.println(patient.getId().getIdPart());
        IParser p = new FhirContext().forDstu2().newJsonParser().setPrettyPrint(true);
        String messageString = p.encodeResourceToString(patient);
        System.out.println(messageString);
        return patient;
    }

    private DocumentReference createUploadDocumentReference(Patient recordTarget, Practitioner author, String custodianId,
            String binaryId, byte[] file) {
        DocumentReference docRef = null;
        try {
            docRef = createDocumentReference(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()), "1.2.840.269649.10",
                    recordTarget, author, custodianId, DocumentReferenceStatusEnum.CURRENT, "CDAR2",
                    "出院病摘_1010222_V101.0_Signed.xml", "text/xml", "N",
                    "http://hl7.org/implement/standards/fhir/v3/Confidentiality/", "normal", "18842-5",
                    "2.16.840.1.113883.6.1", "出院病摘", "115_V101.0", "2.16.886.101.20003.20014", "Discharge Summary Exchange",
                    null, null, null, null, null, null, new Date(), new Date(), binaryId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return docRef;
    }

    private DocumentReference createEMRDocumentReference(Patient recordTarget, Practitioner author, String custodianId,
            String fileName, String binaryId, byte[] file, String confidentialityCode, String confidentialityName,
            String classCode, String className, String typeCode, String typeName) {
        DocumentReference docRef = null;
        try {
            if (confidentialityCode == null)
                confidentialityCode = "N";
            if (confidentialityName == null)
                confidentialityName = "normal";
            docRef = createDocumentReference(null, "1.2.840.269649.10", recordTarget, author, custodianId,
                    DocumentReferenceStatusEnum.CURRENT, "CDAR2", fileName, "text/xml",
                    confidentialityCode, "http://hl7.org/implement/standards/fhir/v3/Confidentiality/", confidentialityName,
                    classCode, "2.16.840.1.113883.6.1", className, typeCode, "2.16.886.101.20003.20014", typeName, null, null,
                    null, null, null, null, new Date(), new Date(), binaryId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return docRef;
    }

    private DocumentReference createDefaultDocumentReference() {
        defaultOrg = readDefaultOrganization();
        defaultPatient = readDefaultPatient();
        defaultPractitioner = readDefaultPractitioner();
        try {
            DocumentReference docRef = createDocumentReference(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()),
                    "1.2.840.269649.10", defaultPatient, defaultPractitioner, defaultOrg.getId().getValue(),
                    DocumentReferenceStatusEnum.CURRENT, "CDAR2", "出院病摘_1010222_V101.0_Signed.xml", 
                    "text/xml", "N", "http://hl7.org/implement/standards/fhir/v3/Confidentiality/",
                    "normal", "18842-5", "2.16.840.1.113883.6.1", "出院病摘", "115_V101.0", "2.16.886.101.20003.20014",
                    "Discharge Summary Exchange", null, null, null, null, null, null, new Date(), new Date(),
                    "Binary/192.168.100.14.695413006364285.1");
            return docRef;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private DocumentReference createDocumentReference(String sheetId, String sheetIdSystem, Patient recordTarget,
            Practitioner author, String custodianId, DocumentReferenceStatusEnum status, String docFormat, String docDesc,
            String mimeType, String confidentialityCode, String confidentialitySystem,
            String confidentialityName, String classCode, String classSystem, String className, String typeCode,
            String typeSystem, String typeName, String eventCode, String eventSystem, String eventName, String facilityCode,
            String facilitySystem, String facilityName, Date serviceStart, Date serviceStop, String binaryUrl) {
        DocumentReference docRef = new DocumentReference();
        docRef.setId(UUID.randomUUID().toString().toUpperCase());
        if (sheetId == null)
            sheetId = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        docRef.addIdentifier().setSystem(sheetIdSystem).setValue(sheetId);
        // TODO: documentUniqueId
        // docRef.setMasterIdentifier("urn:ietf:rfc:3986", documentUniqueId);

        try {
            ContainedDt contained = new ContainedDt();
            List<IResource> containedList = new ArrayList<IResource>();
            containedList.add(recordTarget);
            containedList.add(author);
            contained.setContainedResources(containedList);
            docRef.setContained(contained);
        } catch (Exception e) {
            e.printStackTrace();
        }
        docRef.setCreated(new DateTimeDt(new Date())).addAuthor().setReference(getReferenceId(author.getId()));
        docRef.setCustodian(new ResourceReferenceDt(custodianId));
        docRef.addContent().addFormat(new CodingDt().setCode(docFormat).setDisplay(docDesc));
        docRef.addSecurityLabel().addCoding().setCode(confidentialityCode).setSystem(confidentialitySystem)
                .setDisplay(confidentialityName);

        CodeableConceptDt classCodeDt = new CodeableConceptDt();
        classCodeDt.setText(className).addCoding().setCode(classCode).setSystem(classSystem).setDisplay(className);
        docRef.setClassElement(classCodeDt);
        CodeableConceptDt typeCodeDt = new CodeableConceptDt();
        typeCodeDt.setText(typeName).addCoding().setCode(typeCode).setSystem(typeSystem).setDisplay(typeName);
        docRef.setType(typeCodeDt).setStatus(status);

        Context context = new Context();
        context.addEvent().addCoding().setCode(eventCode).setSystem(eventSystem).setDisplay(eventName);
        CodeableConceptDt facilityTypeCodeDt = new CodeableConceptDt();
        facilityTypeCodeDt.setText(facilityCode).addCoding().setCode(facilityCode).setSystem(facilitySystem)
                .setDisplay(facilityName);
        context.setFacilityType(facilityTypeCodeDt);
        PeriodDt period = new PeriodDt();
        period.setStart(new DateTimeDt(serviceStart)).setEnd(new DateTimeDt(serviceStop));
        context.setPeriod(period);
        docRef.setContext(context);
        context.addRelated().setRef(new ResourceReferenceDt(binaryUrl));

        IParser p = new FhirContext().forDstu2().newJsonParser().setPrettyPrint(true);
        String messageString = p.encodeResourceToString(docRef);
        System.out.println(messageString);
        return docRef;
    }

    private void loadDefaultBinary() {
        Class<FHIRClientDSTU2> clazz = FHIRClientDSTU2.class;
        ClassLoader loader = clazz.getClassLoader();
        InputStream is = null;
        String[] sa = {"出院病摘_1010222_V101.0_Signed.xml", "血液檢驗_1010222_V101.0_Signed.xml", "門診用藥紀錄_1020814_V101.0_Signed.xml",
                "門診病歷單_1020821_121_Signed.xml", "醫療影像及報告_1010221_V101.0_Signed.xml"};
        try {
            is = loader.getResourceAsStream(sa[(int) (Math.random() * (4 - 0 + 1))]);
            defaultBinary = IOUtils.toByteArray(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Binary createBinary() {
        return createBinary(null, "text/xml", defaultBinary, null);
    }

    private Binary createBinary(String id, String contentType, String b64Content, String text) {
        Binary binary = new Binary();
        try {
            if (id == null)
                binary.setId(InetAddress.getLocalHost().getHostAddress() + "." + System.nanoTime() + "."
                        + Thread.currentThread().getId());
            binary.setContentType(contentType);
            binary.setContentAsBase64(b64Content);
            if (text != null && text.trim().length() > 0) {
                NarrativeDt nd = new NarrativeDt();
                nd.setDiv(text);
                binary.setText(nd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return binary;
    }

    private Binary createBinary(String id, String contentType, byte[] content, String text) {
        Binary binary = new Binary();
        try {
            if (id == null)
                binary.setId(genDocumentUniqueId());
            binary.setContentType(contentType);
            binary.setContent(content);
            if (text != null && text.trim().length() > 0) {
                NarrativeDt nd = new NarrativeDt();
                nd.setDiv(text);
                binary.setText(nd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return binary;
    }

    private ListResource createMedicationList(String prescriptionId, String orgId, Patient patient, Practitioner author,
            ListModeEnum mode, List<MedicationOrder> prescriptionList) {
        ListResource generalList = createListResource(prescriptionId, prescriptionId, orgId, "10160-0", "http://loinc.org",
                "History of Medication Use", patient, author, mode, new Date(), null);

        if (prescriptionList != null && prescriptionList.size() > 0) {
            for (MedicationOrder prescription : prescriptionList) {
                ResourceReferenceDt item = new ResourceReferenceDt();
                item.setReference(getReferenceId(prescription.getId())).setDisplay(
                        ((ResourceReferenceDt) prescription.getMedication()).getDisplay().getValue());
                MedicationOrderStatusEnum prescriptionStatus = prescription.getStatusElement().getValueAsEnum();
                List<CodingDt> flagList = new ArrayList<CodingDt>();
                flagList.add(new CodingDt(prescriptionStatus.getSystem(), prescriptionStatus.getCode())
                        .setDisplay(prescriptionStatus.toString()));
                generalList.addEntry().setItem(item).setDate(new DateTimeDt(prescription.getDateWritten())).setFlag(new CodeableConceptDt().setCoding(flagList));            
            }
        }
        return generalList;
    }

    private ListResource createDiagnosisList(String diagnosisId, String orgId, Patient patient, Practitioner author,
            ListModeEnum mode, List<Condition> diagnosisList) {
        ListResource generalList = createListResource(diagnosisId, diagnosisId, orgId, "29548-5", "http://loinc.org",
                "Diagnosis", patient, author, mode, new Date(), null);

        if (diagnosisList != null && diagnosisList.size() > 0) {
            for (Condition diagnosis : diagnosisList) {
                ResourceReferenceDt item = new ResourceReferenceDt();
                item.setReference(getReferenceId(diagnosis.getId())).setDisplay(
                        diagnosis.getCode().getCodingFirstRep().getCode() + " "
                                + diagnosis.getCode().getCodingFirstRep().getDisplay());
                ConditionClinicalStatusCodesEnum status = diagnosis.getClinicalStatusElement().getValueAsEnum();
                List<CodingDt> flagList = new ArrayList<CodingDt>();
                flagList.add(new CodingDt(status.getSystem(), status.getCode()).setDisplay(status.toString()));
                generalList.addEntry().setItem(item).setDate(new DateTimeDt(diagnosis.getDateRecorded())).setFlag(new CodeableConceptDt().setCoding(flagList));
            }
        }
        return generalList;
    }

    private ListResource createObservationList(String observationId, String orgId, Patient patient, Practitioner author,
            ListModeEnum mode, List<Observation> observationList) {
        ListResource generalList = createListResource(observationId, observationId, orgId, "30954-2", "http://loinc.org",
                "Relevant diagnostic tests and/or laboratory data", patient, author, mode, new Date(), null);

        if (observationList != null && observationList.size() > 0) {
            for (Observation observation : observationList) {
                ResourceReferenceDt item = new ResourceReferenceDt();
                item.setReference(getReferenceId(observation.getId())).setDisplay(
                        observation.getCode().getCodingFirstRep().getCode() + " "
                                + observation.getCode().getCodingFirstRep().getDisplay() + " "
                                + ((QuantityDt) observation.getValue()).getCode() + " "
                                + ((QuantityDt) observation.getValue()).getUnit());
                ObservationStatusEnum status = observation.getStatusElement().getValueAsEnum();
                List<CodingDt> flagList = new ArrayList<CodingDt>();
                flagList.add(new CodingDt(status.getSystem(), status.getCode()).setDisplay(status.toString()));
                generalList.addEntry().setItem(item).setDate(new DateTimeDt(observation.getIssued())).setFlag(new CodeableConceptDt().setCoding(flagList));
            }
        }
        return generalList;
    }

    private ListResource createProcedureList(String procedureId, String orgId, Patient patient, Practitioner author,
            ListModeEnum mode, List<Procedure> procedureList) {
        ListResource generalList = createListResource(procedureId, procedureId, orgId, "29554-3", "http://loinc.org",
                "Procedure", patient, author, mode, new Date(), null);

        if (procedureList != null && procedureList.size() > 0) {
            for (Procedure procedure : procedureList) {
                ResourceReferenceDt item = new ResourceReferenceDt();
                item.setReference(getReferenceId(procedure.getId())).setDisplay(
                        procedure.getCode().getCodingFirstRep().getCode() + " "
                                + procedure.getCode().getCodingFirstRep().getDisplay() + " "
                                + procedure.getCode().getText());
                generalList.addEntry().setItem(item).setDate(((PeriodDt)procedure.getPerformed()).getStartElement());
            }
        }
        return generalList;
    }

    private ListResource createListResource(String listId, String identifier, String identifierSystem, String typeCode,
            String typeCodeSystem, String typeCodeDisplay, Patient patient, Practitioner author, ListModeEnum mode,
            Date createDate, List<IResource> entryList) {
        ListResource list = new ListResource();
        list.setId(listId);
        list.addIdentifier().setSystem(identifierSystem).setValue(identifier);
        CodeableConceptDt code = new CodeableConceptDt(typeCodeSystem, typeCode);
        code.setText(typeCodeDisplay);
        list.setCode(code);

        ResourceReferenceDt subject = new ResourceReferenceDt();
        subject.setReference(getReferenceId(patient.getId())).setDisplay(patient.getNameFirstRep().getNameAsSingleString());
        list.setSubject(subject);
        ResourceReferenceDt source = new ResourceReferenceDt();
        source.setReference(getReferenceId(author.getId())).setDisplay(author.getName().getNameAsSingleString());
        list.setSource(source);

        list.setMode(mode);
        list.setDate(new DateTimeDt(createDate));
        if (entryList != null && entryList.size() > 0) {
            for (IResource resource : entryList) {
                ResourceReferenceDt item = new ResourceReferenceDt();
                item.setReference(getReferenceId(resource.getId())).setDisplay(resource.getText().toString());
                list.addEntry().setItem(item);
            }
        }
        return list;
    }

    public MedicationOrder createMedicationOrder(String prescriptionId, String drugId, String drugName,
            String genericName, Date prescriptionDate, Patient patient, Practitioner author, Double durgDose,
            String drugDoseUnit, String durgFrequency, String routeCode, String routeName, Double medicationDays,
            String dosageFormCode, String dosageFormName, String drugBodySiteCode, String drugBodySiteName,
            Double actualAdminAmount, String actualAdminAmountUnit, String asNeededFlag, int continueCount) {
        return createMedicationOrder(prescriptionId, drugId, "2.16.886.101.20003.20014", drugName, genericName,
                prescriptionDate, patient, author, MedicationOrderStatusEnum.ACTIVE, durgDose, drugDoseUnit,
                durgFrequency, routeCode, "2.16.886.101.20003.20014", routeName, medicationDays, dosageFormCode,
                "2.16.840.1.113883.5.85", dosageFormName, drugBodySiteCode, "2.16.886.101.20003.20014", drugBodySiteName,
                actualAdminAmount, actualAdminAmountUnit, asNeededFlag, continueCount);
    }

    private MedicationOrder createMedicationOrder(String prescriptionId, String drugId, String drugIdSystem,
            String drugName, String genericName, Date prescriptionDate, Patient patient, Practitioner author,
            MedicationOrderStatusEnum prescriptionStatus, Double durgDose, String drugDoseUnit, String durgFrequency,
            String routeCode, String routeSystem, String routeName, Double medicationDays, String dosageFormCode,
            String dosageFormSystem, String dosageFormName, String drugBodySiteCode, String drugBodySiteSystem,
            String drugBodySiteName, Double actualAdminAmount, String actualAdminAmountUnit, String asNeededFlag,
            int continueCount) {
        MedicationOrder prescription = new MedicationOrder();
        prescription.addIdentifier().setSystem(drugIdSystem).setValue(drugId);
        prescription.setId(prescriptionId);
        prescription.setDateWritten(new DateTimeDt(prescriptionDate));

        ResourceReferenceDt patientRef = new ResourceReferenceDt();
        patientRef.setReference(getReferenceId(patient.getId())).setDisplay(patient.getNameFirstRep().getNameAsSingleString());
        prescription.setPatient(patientRef);
        ResourceReferenceDt prescriberRef = new ResourceReferenceDt();
        prescriberRef.setReference(getReferenceId(author.getId())).setDisplay(author.getName().getNameAsSingleString());
        prescription.setPrescriber(prescriberRef);

        prescription.setStatus(prescriptionStatus);
        NarrativeDt text = new NarrativeDt();
        text.setDiv(StringEscapeUtils.escapeXml10(drugName + "(" + genericName + ")"));
        prescription.setText(text);

        CodeableConceptDt route = new CodeableConceptDt(routeSystem, routeCode);
        route.setText(routeName).getCodingFirstRep().setDisplay(routeName);
        CodeableConceptDt dosageForm = new CodeableConceptDt(dosageFormSystem, dosageFormCode);
        dosageForm.setText(dosageFormName).getCodingFirstRep().setDisplay(dosageFormName);
        CodeableConceptDt bodySite = new CodeableConceptDt(drugBodySiteSystem, drugBodySiteCode);
        bodySite.setText(drugBodySiteName).getCodingFirstRep().setDisplay(drugBodySiteName);
        RatioDt frequency = new RatioDt();
        frequency.setElementSpecificId(durgFrequency);
        prescription.addDosageInstruction().setDose(new QuantityDt(durgDose).setUnits(drugDoseUnit))
                .setMethod(dosageForm).setRoute(route).setSite(bodySite).setText(String.valueOf(medicationDays))
                .setRate(frequency).setAsNeeded(new StringDt(asNeededFlag));
        // .setMaxDosePerPeriod(null).setRate(null)
        DispenseRequest dispense = new DispenseRequest();
        dispense.setNumberOfRepeatsAllowed(continueCount); // 連續處方箋註記3
        SimpleQuantityDt quantity = new SimpleQuantityDt(actualAdminAmount);
        quantity.setUnit(actualAdminAmountUnit);
        dispense.setQuantity(quantity); // 實際給藥總量&單位
        prescription.setDispenseRequest(dispense);

        ResourceReferenceDt medicationRef = new ResourceReferenceDt();
        medicationRef.setDisplay(StringEscapeUtils.escapeXml10(drugName + "(" + genericName + ")"));
        medicationRef.setResource(createNHIMedication(drugId, drugName, genericName, durgDose, drugDoseUnit, dosageFormCode,
                dosageFormName));
        prescription.setMedication(medicationRef);
        // prescription.setReason(null);
        return prescription;
    }

    private Medication createNHIMedication(String drugId, String drugName, String genericName, Double durgDose,
            String drugDoseUnit, String dosageFormCode, String dosageFormName) {
        return createMedication(drugId, "http://www.nhi.gov.tw/", drugName, genericName, durgDose, drugDoseUnit,
                dosageFormCode, "http://www.nhi.gov.tw/", dosageFormName);
    }

    private Medication createMedication(String drugId, String drugSystem, String drugName, String genericName, Double durgDose,
            String drugDoseUnit, String dosageFormCode, String dosageFormSystem, String dosageFormName) {
        Medication medication = new Medication();
        CodeableConceptDt code = new CodeableConceptDt(drugSystem, drugId);
        code.getCodingFirstRep().setDisplay(drugName);
        medication.setCode(code);

        // medication.setIsBrand(true);
        // Organization organization = new Organization();
        // organization.setName("五福化學製藥廠有限公司");
        // medication.setManufacturer(new ResourceReferenceDt(organization));
        // medication.setKind(MedicationKindEnum.PRODUCT);

        Product product = new Product();
        CodeableConceptDt form = new CodeableConceptDt(dosageFormSystem, dosageFormCode);
        form.getCodingFirstRep().setDisplay(dosageFormName);
        product.setForm(form);
        product.addIngredient().setAmount(new RatioDt().setNumerator(new QuantityDt(durgDose).setUnit(drugDoseUnit)));
        medication.setProduct(product);
        return medication;
    }

    public Condition createDiagnosisCondition(String id, String identifier, String identifierSystem, Patient patient,
            Practitioner author, Date assertDate, String code, String codeName, ConditionClinicalStatusCodesEnum status,
            String encounterRef, String encounterName, String comment, String text) {
        return createCondition(id, identifier, identifierSystem, getReferenceId(patient.getId()), patient.getNameFirstRep()
                .getNameAsSingleString(), getReferenceId(author.getId()), author.getName().getNameAsSingleString(), assertDate,
                "diagnosis", "http://www.hl7.org/implement/standards/fhir/condition-definitions.html#Condition.category", "診斷",
                code, "2.16.840.1.113883.6.2", codeName, status, encounterRef, encounterName, comment, text);
    }

    private Condition createCondition(String id, String identifier, String identifierSystem, String subjectRef,
            String subjectName, String asserterRef, String asserterName, Date assertDate, String categoryCode,
            String categorySystem, String categoryName, String code, String codeSystem, String codeName,
            ConditionClinicalStatusCodesEnum status, String encounterRef, String encounterName, String comment, String text) {
        Condition condition = new Condition();
        condition.addIdentifier().setSystem(identifierSystem).setValue(identifier);
        condition.setId(id);
        condition.setPatient(new ResourceReferenceDt(subjectRef).setDisplay(subjectName));
        condition.setAsserter(new ResourceReferenceDt(asserterRef).setDisplay(asserterName));
        condition.setDateRecorded(new DateDt(assertDate));

        BoundCodeableConceptDt<ConditionCategoryCodesEnum> conditionCategory = new BoundCodeableConceptDt<ConditionCategoryCodesEnum>();
        conditionCategory.addCoding(new CodingDt(categorySystem, categoryCode));
        conditionCategory.getCodingFirstRep().setDisplay(categoryName);
        condition.setCategory(conditionCategory);
        CodeableConceptDt conditionCode = new CodeableConceptDt(codeSystem, code);
        conditionCode.getCodingFirstRep().setDisplay(codeName);
        condition.setCode(conditionCode);
        condition.setClinicalStatus(status);
        condition.setEncounter(new ResourceReferenceDt(encounterRef).setDisplay(encounterName));
        condition.setNotes(comment);
        NarrativeDt div = new NarrativeDt();
        div.setDiv(text);
        condition.setText(div);
        return condition;
    }

    public Observation createLABObservation(String id, String identifier, String identifierSystem, Patient patient,
            Practitioner author, Date observationDate, String bodySiteCode, String bodySiteSystem, String bodySiteName,
            String methodCode, String methodSystem, String methodName, String nameCode, String nameSystem, String nameName,
            String specimenTypeCode, String specimenTypeSystem, String specimenTypeName,
            Date specimenReceivedTime, ObservationStatusEnum status, String value, String valueUnit, String refString,
            String refLow, String refHigh, String comment, String text) {
        return createObservation(id, identifier, identifierSystem, getReferenceId(patient.getId()), patient.getNameFirstRep()
                .getNameAsSingleString(), getReferenceId(author.getId()), author.getName().getNameAsSingleString(),
                observationDate, bodySiteCode, bodySiteSystem, bodySiteName, methodCode, methodSystem, methodName, nameCode,
                nameSystem, nameName, specimenTypeCode, specimenTypeSystem, specimenTypeName,
                specimenReceivedTime, status, value, valueUnit, refString, refLow, refHigh, comment, text);
    }

    private Observation createObservation(String id, String identifier, String identifierSystem, String patientRef,
            String paitnetName, String performerRef, String performerName, Date observationDate, String bodySiteCode,
            String bodySiteSystem, String bodySiteName, String methodCode, String methodSystem, String methodName,
            String codeCode, String codeSystem, String nameName, String specimenTypeCode, String specimenTypeSystem,
            String specimenTypeName, Date specimenReceivedTime, ObservationStatusEnum status,
            String value, String valueUnit, String refString, String refLow, String refHigh, String comment, String text) {
        Observation observation = new Observation();
        observation.addIdentifier().setSystem(identifierSystem).setValue(identifier);
        observation.setId(id);
        observation.setSubject(new ResourceReferenceDt(patientRef).setDisplay(paitnetName));
        observation.addPerformer().setReference(performerRef).setDisplay(performerName);
        observation.setIssued(new InstantDt(observationDate));

        CodeableConceptDt bodySite = new CodeableConceptDt(bodySiteSystem, bodySiteCode);
        bodySite.getCodingFirstRep().setDisplay(bodySiteName);
        observation.setBodySite(bodySite);
        CodeableConceptDt method = new CodeableConceptDt(methodSystem, methodCode);
        method.getCodingFirstRep().setDisplay(methodName);
        observation.setMethod(method);
        CodeableConceptDt code = new CodeableConceptDt(codeSystem, codeCode);
        code.getCodingFirstRep().setDisplay(nameName);
        observation.setCode(code);

        ResourceReferenceDt specimenRef = new ResourceReferenceDt();
        specimenRef.setDisplay(StringEscapeUtils.escapeXml10(specimenTypeCode + "(" + specimenTypeName + ")"));
        specimenRef.setResource(createSpecimen(null, null, patientRef, paitnetName, specimenTypeCode, specimenTypeSystem,
                specimenTypeName, specimenReceivedTime, null));
        observation.setSpecimen(specimenRef);
        observation.setStatus(status);
        observation.setValue(new QuantityDt().setCode(value).setUnit(valueUnit));
        SimpleQuantityDt quantityHigh = new SimpleQuantityDt();
        quantityHigh.setCode(refHigh);
        SimpleQuantityDt quantityLow = new SimpleQuantityDt();
        quantityLow.setCode(refLow);
        observation.addReferenceRange().setHigh(quantityHigh).setLow(quantityLow).setElementSpecificId(refString);
        observation.setComments(comment);
        NarrativeDt div = new NarrativeDt();
        div.setDiv(text);
        observation.setText(div);
        return observation;
    }

    private Specimen createSpecimen(String accessionId, String accessionSystem, String subjectRef, String subjectName,
            String typeCode, String typeSystem, String typeName, Date receivedTime, String text) {
        Specimen specimen = new Specimen();
        specimen.setAccessionIdentifier(new IdentifierDt(accessionSystem, accessionId));
        CodeableConceptDt type = new CodeableConceptDt(typeSystem, typeCode);
        type.getCodingFirstRep().setDisplay(typeName);
        specimen.setType(type);

        specimen.setReceivedTime(new DateTimeDt(receivedTime));
        specimen.setSubject(new ResourceReferenceDt(subjectRef).setDisplay(subjectName));
        NarrativeDt div = new NarrativeDt();
        div.setDiv(text);
        specimen.setText(div);
        return specimen;
    }

    public Procedure createOPDProcedure(String id, String identifier, String identifierSystem, Patient patient,
            Practitioner author, Date procedureStartDate, Date procedureEndDate, String bodySiteCode, String bodySiteSystem,
            String bodySiteName, String typeCode, String typeSystem, String typeName, String procedureAmount, String followUp,
            String note, String outcome, String text) {
        return createProcedure(id, identifier, identifierSystem, getReferenceId(patient.getId()), patient.getNameFirstRep()
                .getNameAsSingleString(), getReferenceId(author.getId()), author.getName().getNameAsSingleString(),
                procedureStartDate, procedureEndDate, bodySiteCode, bodySiteSystem, bodySiteName, typeCode, typeSystem,
                typeName, procedureAmount, "Encounter/ABM", "ambulatory", followUp, note, outcome, text);
    }

    private Procedure createProcedure(String id, String identifier, String identifierSystem, String patientRef,
            String patientName, String performerRef, String performerName, Date procedureStartDate, Date procedureEndDate,
            String bodySiteCode, String bodySiteSystem, String bodySiteName, String procedureCode, String codeSystem,
            String codeName, String procedureAmount, String encounterRef, String encounterName, String followUp, String note,
            String outcome, String text) {
        Procedure procedure = new Procedure();
        procedure.addIdentifier().setSystem(identifierSystem).setValue(identifier);
        procedure.setId(id);
        procedure.setSubject(new ResourceReferenceDt(patientRef).setDisplay(patientName));
        procedure.addPerformer().setActor(new ResourceReferenceDt(performerRef).setDisplay(performerName));
        procedure.setPerformed(new PeriodDt().setStart(new DateTimeDt(procedureStartDate)).setEnd(new DateTimeDt(procedureEndDate)));

        CodeableConceptDt bodySite = new CodeableConceptDt(bodySiteSystem, bodySiteCode);
        bodySite.getCodingFirstRep().setDisplay(bodySiteName);
        procedure.addBodySite().addCoding().setCode(bodySiteCode).setSystem(bodySiteSystem).setDisplay(bodySiteName);
        CodeableConceptDt code = new CodeableConceptDt(codeSystem, procedureCode);
        code.setText(procedureAmount).getCodingFirstRep().setDisplay(codeName);
        procedure.setCode(code);
        procedure.setEncounter(new ResourceReferenceDt(encounterRef).setDisplay(encounterName));
        procedure.addFollowUp().setElementSpecificId(followUp);
        List<AnnotationDt> noteList = new ArrayList<AnnotationDt>();
        noteList.add(new AnnotationDt().setText(note));
        procedure.setNotes(noteList);
        procedure.setOutcome(new CodeableConceptDt().addCoding(new CodingDt().setCode(outcome)));
        // procedure.addReport();

        NarrativeDt div = new NarrativeDt();
        div.setDiv(text);
        procedure.setText(div);
        return procedure;
    }

    public Encounter createBasicEncounter(String id, String identifier, EncounterClassEnum encounterClass, String text) {
        return createEncounter(id, identifier, "2.16.840.1.113883.1.11.13955", null, null, null, null, encounterClass, null,
                text);
    }

    private Encounter createEncounter(String id, String identifier, String identifierSystem, String patientRef,
            String patientName, String participantRef, String participantName, EncounterClassEnum encounterClass,
            EncounterStateEnum status, String text) {
        Encounter encounter = new Encounter();
        encounter.addIdentifier().setSystem(identifierSystem).setValue(identifier);
        encounter.setId(id);
        encounter.setPatient(new ResourceReferenceDt(patientRef).setDisplay(patientName));
        encounter.addParticipant().setIndividual(new ResourceReferenceDt(participantRef).setDisplay(participantName));
        encounter.setClassElement(encounterClass).setStatus(status);
        NarrativeDt div = new NarrativeDt();
        div.setDiv(text);
        encounter.setText(div);
        return encounter;
    }
    
    public DiagnosticReport createEverBioTestDiagnosticReport(String id, String identifier, String codedDiagnosis1, 
            String codedDiagnosis2,String codedDiagnosis3, String conclusion) {
        return createDiagnosticReport(id, identifier, "1.2.840.269649", "Patient/52", "策仕仁", "Practitioner/51", "測試醫師", 
                codedDiagnosis1, "X", codedDiagnosis2, "Y", codedDiagnosis3, "Z", "RAD", "27395391", conclusion, null);
    }
    
    private DiagnosticReport createDiagnosticReport(String id, String identifier, String identifierSystem, String patientRef,
            String patientName, String performerRef, String performerName, String codedDiagnosis1, String codedDiagnosis1Id, 
            String codedDiagnosis2, String codedDiagnosis2Id, String codedDiagnosis3, String codedDiagnosis3Id,
            String serviceCategory, String codedDiagnosisSystem, String conclusion, String text) {
        DiagnosticReport diagnosticReport = new DiagnosticReport();
        diagnosticReport.setId(id);
        diagnosticReport.addIdentifier().setValue(identifier).setSystem(identifierSystem);
        diagnosticReport.setSubject(new ResourceReferenceDt(patientRef).setDisplay(patientName));
        diagnosticReport.setPerformer(new ResourceReferenceDt(performerRef).setDisplay(performerName));
        diagnosticReport.setStatus(DiagnosticReportStatusEnum.FINAL);
        diagnosticReport.setIssued(new InstantDt(new Date()));
        //diagnosticReport.setServiceCategory(DiagnosticServiceSectionCodesEnum.valueOf(serviceCategory));
        diagnosticReport.setConclusion(conclusion);
        CodeableConceptDt codeX = new CodeableConceptDt();
        codeX.addCoding().setCode(codedDiagnosis1Id).setSystem(codedDiagnosisSystem).setDisplay(codedDiagnosis1);
        CodeableConceptDt codeY = new CodeableConceptDt();
        codeY.addCoding().setCode(codedDiagnosis2Id).setSystem(codedDiagnosisSystem).setDisplay(codedDiagnosis2);
        CodeableConceptDt codeZ = new CodeableConceptDt();
        codeZ.addCoding().setCode(codedDiagnosis3Id).setSystem(codedDiagnosisSystem).setDisplay(codedDiagnosis3);
        diagnosticReport.addCodedDiagnosis(codeX);
        diagnosticReport.addCodedDiagnosis(codeY);
        diagnosticReport.addCodedDiagnosis(codeZ);
        
        NarrativeDt textDiv = new NarrativeDt();
        textDiv.setDiv(text);
        diagnosticReport.setText(textDiv);
        
        IParser p = new FhirContext().forDstu2().newJsonParser().setPrettyPrint(true);
        String messageString = p.encodeResourceToString(diagnosticReport);
        System.out.println(messageString);
        return diagnosticReport;
    }
    
    private String getSha1(byte[] bytes) throws Exception {
        if (bytes == null)
            throw new Exception("Sha1Bean:getSha1: byteStream is null");
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (Exception e) {
        }
        byte[] sha1 = md.digest(bytes);

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < sha1.length; i++) {
            String h = Integer.toHexString(sha1[i] & 0xff);
            if (h.length() == 1)
                h = "0" + h;
            buf.append(h);
        }
        return new String(buf);
    }

    private String genDocumentUniqueId() {
        try {
            return new String(InetAddress.getLocalHost().getHostAddress() + "." + System.nanoTime() + "."
                    + Thread.currentThread().getId());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
