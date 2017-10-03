# My Virtual Tour

## Requirements
* You must have the following installed:
  * Java 8
  * Maven

## Quick Start
1. Build the project using `mvn install`.
2. Run the web application via `mvn spring-boot:run`. Optionally, you can also start the application via `ph.edu.tsu.tour.Application#main(String[])`.
3. Profit!

Default Credentials:

    Username: admin
    Password: admin

## Configuration
Feel free to modify the `application.properties` file to configure the application to your liking.

To see other Spring Boot-specific configurable properties, you may consult 
[this page](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html).

### Storage

As of now, only the following storage options are supported:

- [Amazon S3](https://aws.amazon.com/s3/)
- [Dropbox](https://www.dropbox.com/)

The following properties are also supported:

* `application.storage.default-directory`: sets the default storage directory.
* `application.storage.dropbox.access-token`
* `application.storage.amazon.s3.secret-key`
* `application.storage.amazon.s3.access-key`
* `application.storage.amazon.s3.region`
* `application.storage.amazon.s3.endpoint`
* `application.storage.amazon.s3.max-retries`

### Domain Objects

#### Points of Interest

##### Images

The following properties are also supported:

* `application.domain.poi.image.resize`: boolean property indicating if image should be resized or not.
* `application.domain.poi.image.max-width`: the max width of an image in pixels. If resize is set to true, 
  then this field should be specified. Otherwise, it's optional.
* `application.domain.poi.image.max-height`: the max height of an image in pixels. If resize is set to true, 
  then this field should be specified. Otherwise, it's optional.
* `application.domain.poi.image.quality`: the quality of the image. Input should range from `0` to `1`, 
  with `1` having the most optimal quality.
* `application.domain.poi.image.preview.resize`: boolean property indicating if preview image should be resized or not.
* `application.domain.poi.image.preview.max-width`: the max width of a preview image in pixels. 
  If resize is set to true, then this field should be specified. Otherwise, it's optional.
* `application.domain.poi.image.preview.max-height`: the max height of a preview image in pixels. 
  If resize is set to true, then this field should be specified. Otherwise, it's optional.
* `application.domain.poi.image.preview.quality`: the quality of the preview image. Input should range from `0` to `1`, 
  with `1` having the most optimal quality.
  
### Maps

#### Mapbox

Internally, this web application uses the [Mapbox](http://www.mapbox.com) APIs. To use the RESTful map APIs exposed by 
this application, you must configure the Mapbox access token in the `application.properties` file via the 
`application.map.mapbox.access-token` key.

## References
* [Maven](https://maven.apache.org)
* [Spring Boot](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)