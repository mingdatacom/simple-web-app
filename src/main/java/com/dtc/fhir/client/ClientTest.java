package com.dtc.fhir.client;

import java.util.List;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.BundleEntry;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Person;
import ca.uhn.fhir.model.dstu2.resource.Person.Link;
import ca.uhn.fhir.rest.client.IGenericClient;

public class ClientTest {

	public static void main(String[] args) {
		
		FhirContext ctx = new FhirContext();
		IGenericClient client = ctx.newRestfulGenericClient("http://fhirtest.uhn.ca/baseDstu2");
		Bundle bundle = client.search().forResource(Person.class)
				.include(new Include("Person:link"))
				.prettyPrint()
				.execute();
		if (null != bundle.getLinkNext() ) {
			System.out.println("link next: " + bundle.getLinkNext());
		}
		for (Person entry : bundle.getResources(Person.class)) {
			List links = entry.getLink();
			if (null != links && links.size() > 0) {
				System.out.println("person has link: " + entry.getId());
			}
		}
			
		Person person = client.read().resource(Person.class).withId("42156").execute();
		List<Person.Link> links = person.getLink();
		for (Link link : links) {
			System.out.println("link: " + link.getClass() + " : " + link.getElementSpecificId());
		}
	}

	static void dtcfhirclient() {
		FHIRClientDSTU2 client = new FHIRClientDSTU2("http://192.168.1.21:8088/baseDstu2");
		List<Patient> patients = client.searchPatientByIdentifier("A100000001");
		if (patients != null)
			System.out.println("number of patients with identifier A100000001: " + patients.size());
		else 
			System.out.println("A100000001 not found");
		patients = client.searchPatientById("A100000001");
		if (patients != null)
			System.out.println("search by id A100000001: " + patients.size());
		else 
			System.out.println("A100000001 not found");
	}
}
