# Forge REST Service

This project provides a REST service implemented via JAX-RS for interacting with Forge so that web based tooling can be created.

### Building

[Install maven](http://maven.apache.org/download.cgi) then run:

    mvn install
    cd main
    mvn compile exec:java

The REST API should be running now at [http://localhost:8588/](http://localhost:8588/) which should list some APIs you can invoke to try it out.

#### Invoking commands

To invoke a command **my-command* POST an ExecutionRequest as JSON to the URI **http://localhost:8588/api/forge/commands/my-command** of the form:

```
{
  "resource": "someSelectedFolderOrFileName",
  "items":  {
    "foo": "bar",
  },
  "promptQueue": ["something"]
}
```

Some commands don't require any input; usually though the selected resource is required; particularly for project related commands which need to know the folder of the project.


### Modules

* **core** provides the core library
* **main** provides a stand alone executable main()
* **war** provides a WAR that can be deployed in wildfly et al