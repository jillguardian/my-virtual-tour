# Configuring the application

Feel free to modify the `server/src/main/resources/application.properties` file
to configure the application to your liking.

To see other Spring Boot-specific configurable properties, you may consult 
[this page](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html).

## Profile

Available profiles:
- `default`
- `development`

Set the active Spring profile by setting the value of the `SPRING_PROFILES_ACTIVE` environment property.

## Storage

As of now, only the following storage options are supported:

- [Amazon S3](https://aws.amazon.com/s3/)
- [Dropbox](https://www.dropbox.com/)

The following properties are also supported:

* `storage.default-directory`: sets the default storage directory.
* `storage.dropbox.access-token`
* `storage.amazon.s3.secret-key`
* `storage.amazon.s3.access-key`
* `storage.amazon.s3.region`
* `storage.amazon.s3.endlocationnt`
* `storage.amazon.s3.max-retries`

If you would like to switch to a different storage service, simply set the `storage.default-directory`
property's path with a different prefix.

Using the `dropbox` prefix will allow the application to use the Dropbox storage service:
```
storage.default-directory=dropbox://<your-base-directory>
```

Using the `s3` prefix will allow the application to use Amazon S3:
```
storage.default-directory=s3://<your-base-directory>
```

## Location images

The following properties are also supported:

* `location.image.resize`: boolean property indicating if image should be resized or not.
* `location.image.max-width`: the max width of an image in pixels. If resize is set to true, 
  then this field should be specified. Otherwise, it's optional.
* `location.image.max-height`: the max height of an image in pixels. If resize is set to true, 
  then this field should be specified. Otherwise, it's optional.
* `location.image.quality`: the quality of the image. Input should range from `0` to `1`, 
  with `1` having the most optimal quality.
* `location.image.preview.resize`: boolean property indicating if preview image should be resized or not.
* `location.image.preview.max-width`: the max width of a preview image in pixels. 
  If resize is set to true, then this field should be specified. Otherwise, it's optional.
* `location.image.preview.max-height`: the max height of a preview image in pixels. 
  If resize is set to true, then this field should be specified. Otherwise, it's optional.
* `location.image.preview.quality`: the quality of the preview image. Input should range from `0` to `1`, 
  with `1` having the most optimal quality.
  
## Maps

Internally, this web application uses the [Mapbox](http://www.mapbox.com) APIs.

To use the RESTful map APIs exposed by this application, you must configure the Mapbox access token
in the `application.properties` file via the `mapbox.access-token` key.