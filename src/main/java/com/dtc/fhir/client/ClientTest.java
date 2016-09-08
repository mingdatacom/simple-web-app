package com.dtc.fhir.client;

import java.util.List;

import ca.uhn.fhir.model.dstu2.resource.Patient;

public class ClientTest {

	public static void main(String[] args) {
		FHIRClientDSTU2 client = new FHIRClientDSTU2();
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
