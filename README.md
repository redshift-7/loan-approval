# Loan Approval [![Build Status](https://travis-ci.com/redshift-7/loan-approval.svg?branch=deploy)](https://travis-ci.com/redshift-7/loan-approval)

Create a RESTful service that would enable loan preparators to send loan contracts to loan managers for approval. Loan preparator must specify the customer’s ID and the amount that customer wants to loan. 
Loan preparator must also specify which managers need to approve it . After the specified managers have added their approvals the contract will be automatically sent to the customer.
 
Create a simple service that would allow to make the following actions:
1. Creating a loan amount approval request with the following properties:
	* customer’s ID (text, must be in a pattern XX-XXXX-XXX where X is either number or a letter)
	* loan amount (number)
	* approvers (list of usernames (text), up to 3)
 
2. Making a decision that has at least the following inputs: 
	* loan manager’s username 
	* customer’s ID
 
4. Return statistics of contracts that were sent to the customers during a period that is configured in the application (default is 60 seconds). Endpoint must return the following:
	* count -  count of sent contracts 
	* sum - sum of all the loan amounts
	* avg - average loan amount
	* max - biggest loan amount
	* min - smallest loan amount 
 
Notes:
	* There can be only one pending loan request for one customer
	* There can be multiple contracts sent to the service at the same time.
	* Think about memory usage and performance when choosing collections
	* Front-end is not required and will not give any extra points besides better demonstration of the working service.
 
Technical requirements:
	* Must use Gradle
	* Written in Java
	* Demonstrates object oriented paradigm
	* Follows clean code principles
	* Must be production ready
	* Service should not use any databases (in-memory nor any persistent ones), only Java collections.
	* Request input validations must be present and correct error messages returned with correct HTTP status code
	* Code must be covered with unit tests
 
 
Bonus points:
* API tests
* Simple docker container for running the service

----------------

## How to build and run:

```$batch
./gradlew.bat bootBuildImage --imageName=loan-approval
docker run -p 8080:8080 -t docker.io/library/loan-approval:0.0.1-SNAPSHOT
```
or
```batch
./gradlew.bat clean build
docker build --build-arg JAR_FILE=target/*.jar -t loan-approval .
docker run -p 8080:8080 -t loan-approval
```


### API Calls Example


- `POST http://localhost:8080/api/loans/approval-request`

```json
{
  "customerId" : "XX-XXXX-X5X",
  "loanAmount" : 123.45,
  "approvers" : [ 
    "Senior Approver"
   ],
  "timestamp" : null,
  "decisionState" : null
}
```

- `POST http://localhost:8080/api/loans/decision`

```json
{
    "customerId":"XX-XXXX-X5X",
    "approverUsername":"Uncle Bob",
    "state":"APPROVED"
}
```
- `POST http://localhost:8080/api/loans/decision`
```json
{
    "customerId":"XX-XXXX-X5X",
    "approverUsername":"Uncle Bob",
    "state":"DECLINED"
}
```

- `GET http://localhost:8080/api/loans/statistics`
