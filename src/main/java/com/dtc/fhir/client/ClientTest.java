package com.dtc.fhir.client;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

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
	private static CloseableHttpClient ourHttpClient;
	
	public static void main(String[] args) {
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(5000, TimeUnit.MILLISECONDS);
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setConnectionManager(connectionManager);
		ourHttpClient = builder.build();
		
		FhirContext ctx = new FhirContext();
		IGenericClient client = ctx.newRestfulGenericClient("http://fhirtest.uhn.ca/baseDstu2");
			
		Bundle bundle = client.search().forResource(Person.class)
				.include(new Include("Person:link"))
				.prettyPrint()
				.execute();
		System.out.println("link1: " + bundle.getLinkNext());
		System.out.println("link2: " + bundle.getLinkNext());
		while(null != bundle && null != bundle.getResources(Person.class) && bundle.getResources(Person.class).size() > 0) {
			System.out.println("number of resources: " + bundle.getResources(Person.class).size());
			for (Person entry : bundle.getResources(Person.class)) {
				List links = entry.getLink();
				if (null != links && links.size() > 0) {
					System.out.println("person: " + entry.getId() + " :has link: " + entry.getLink().get(0));
				}
			}
			// load next page
			if (null != bundle.getLinkNext().getValue()) {
				System.out.println("next link: " + bundle.getLinkNext());
				bundle = client.loadPage().next(bundle).execute();
			} else {
				bundle = null;
			}
		}
	}

	static void readPerson(IGenericClient client) {
		Person person = client.read().resource(Person.class).withId("42156").execute();
		List<Person.Link> links = person.getLink();
		for (Link forlink : links) {
			System.out.println("link: " + forlink.getClass() + " : " + forlink.getElementSpecificId());
		}
		
	}
	
	/**
	 * http://fhirtest.uhn.ca/baseDstu2?_getpages=dc99bcce-a011-4672-b369-d9d3bbf57d98&_getpagesoffset=30&_count=10&_pretty=true&_bundletype=searchset
	 * @param url
	 */
	static void callSingleUrl(FhirContext ctx, String url) {
		Bundle bundle;
		String link = url;
		HttpGet httpGet = new HttpGet(link);
		HttpResponse status;
		try {
			status = ourHttpClient.execute(httpGet);
			String responseContent = IOUtils.toString(status.getEntity().getContent());
			//System.out.println("responsecontent: " + responseContent);
			bundle = ctx.newJsonParser().parseBundle(responseContent);
			System.out.println("number of resources: " + bundle.getResources(Person.class).size());
			for (Person entry : bundle.getResources(Person.class)) {
				List links = entry.getLink();
				if (null != links && links.size() > 0) {
					System.out.println("person: " + entry.getId() + " :has link: " + entry.getLink().get(0));
				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void searchPersons(FhirContext ctx, IGenericClient client) {
		Bundle bundle = client.search().forResource(Person.class)
				.include(new Include("Person:link"))
				.prettyPrint()
				.execute();
		
		while(null != bundle.getLinkNext()) {
			if (null != bundle.getLinkNext() ) {
				System.out.println("link next: " + bundle.getLinkNext());
			}
			
			String link = bundle.getLinkNext().getValue();
			HttpGet httpGet = new HttpGet(link);
			HttpResponse status;
			try {
				status = ourHttpClient.execute(httpGet);
				String responseContent = IOUtils.toString(status.getEntity().getContent());
				//System.out.println("responsecontent: " + responseContent);
				System.out.println("number of resources: " + bundle.getResources(Person.class).size());
				for (Person entry : bundle.getResources(Person.class)) {
					List links = entry.getLink();
					if (null != links && links.size() > 0) {
						System.out.println("person: " + entry.getId() + " :has link: " + entry.getLink().get(0));
					}
				}
				bundle = ctx.newJsonParser().parseBundle(responseContent);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
