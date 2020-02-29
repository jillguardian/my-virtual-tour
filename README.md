# My Virtual Tour

My Virtual Tour is a mobile-friendly, web-based route planner for tourists wishing to visit churches in Tarlac.
It is a two-part application; with an interface for tourists, and another for destination content managers.

This repository contains the source code for the back-end web services required by the application.
It is written in Java, and uses Spring Boot.

## RESTful APIs

- Users
    - Register
    - Verify
    - Update
    - Reset passwords
- Tours (secured)
    - Save
    - Get
    - Delete
- Maps
    - Get directions
    - Sort destinations
    - Get nearest destination
- Churches
    - Get
    - Get all

## WebSocket destinations

- `/topic/location-updates`

    Where real-time content updates to churches are published. 

## Web views

- Churches (secured)
    - Save and update
        - Contact
        - Location
        - History
        - Architecture
        - Schedules
        - Facilities
        - Nearby establishments
        - Images
    - Get
    - Get all
    - Delete
        - Images
        - Churches
- Users
    - Verification success page
    - Change password page
- Administrators (secured)
    - Save
    - Get
    - Get all
    - Delete

## References

* [Maven](https://maven.apache.org)
* [Spring Boot](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)