# Setting up a repository ##

> IMPORTANT NOTES:  

> Replace **https://github.com/** with **git@github:** if you use SSH keys
> rather than user:password credentials for authentication at github. Depending on your setup all URLs in this
> tutorial either read `https://github.com/okotta` or `git@github.com:okotta`

> NEVER push changes to the template! Read more about the recommended (read:only) way of handling
> template changes in the respective section (at the end of this file)
 
 
The steps to take for creating a new repository (explained in more detail below)

   * Clone template
   * Create <new repository> on github
   * Add <new-repository> to list of remote

## Clone the template

Create a new repository based on the existing template.

    $ git clone --origin template  https://github.com/okotta/scalatra-rest-template.git <new-repo>
    $ cd <new-repo>

The new repository will have a remote named "template" that points to the remote location of 
the template.

`git remote -v` shows the current setup of remote references


    $ template    https://github.com/okotta/scalatra-rest-template.git (fetch)
    $ template    https://github.com/okotta/scalatra-rest-template.git (push)

## Create and add remote

Create an EMPTY (**DO NOT INITIALIZE**) repository on github (this means to create a real repository using the github website)
and add its remote location

    $ git remote add origin  https://github.com/okotta/<new-repo>.git

`git remote -v` should now show two different remote references mapped to their respective alias

    $ origin    git@github.com:okotta/<new-repo>.git (fetch)  
    $ origin    git@github.com:okotta/<new-repo>.git (push)  
    $ template    git@github.com:okotta/scalatra-rest-template.git (fetch)  
    $ template    git@github.com:okotta/scalatra-rest-template.git (push)  

Create the master branch and set the upstream with `git push -u origin master`

You can use `git pull template <branch>` to get updates made to the template and `git push origin <branch>`
to push your changes to the respective branch. NEVER push changes to the template! If template code needs to be
changed, the recommended workflow is to make the changes in the template repository and pull them into the 
dependent repositories using `git pull template <branch>`

If you do not specify a branch then you will get the following error message:

```
You asked to pull from the remote 'template', but did not specify
a branch. Because this is not the default configured remote
for your current branch, you must specify a branch on the command line.
```

> IMPORTANT  
> Whenever you check out a repository based on a template

# Working with the repo


## SBT

The template comes with a custom sbt launcher that can but does not need to be used. In any case sbt can be started
with a parameter to tell the application which config file to use. Without this parameter the default is 
application.conf (as described in [[Typesafe Config Guide]](https://github.com/typesafehub/config#standard-behavior)

For testing environment this is

    $ ./sbt -Dconfig.resource=application.testing.conf

## Test your setup ##

To compile the project and run the included test suite

    $ ;clean;test

To launch the app in a jetty container do

    $ container:start
    
Open [http://localhost:8080/api-docs](http://localhost:8080/api-docs) in a browser to verify that the container
is up and running. 

> You need a mongodb running on the port as specified in `application.conf` or the config file you are usoing.

## Development ##

Activate automatic recompile and update of server:

    $ ~ ;copy-resources;aux-compile

Run all tests

    $ test 

Run unit tests only

    $ test-only com.github.bennidi.scalatrest.unit.* 
   
Run integration tests only

    $ test-only com.github.bennidi.scalatrest.integration.*    

Run tests with coverage instrumentation. Reports are found in `/target/scala-xxx/coverage-reports` folder

    $ ;coverage;test;coverageReport 
    
Generate documentation (ScalaDoc). Generated site will be located in `/target/scala-xxx/[api|test-api]` folder
    
    $ doc
    $ test:doc

OPTIONAL: The stack uses Swagger for documentation of APIs. To make use of the Swagger features, you can use Swagger UI. Either you use it online or you maintain a local copy (recommended). Clone Swagger-UI into a directory different from your repository.

    $ git clone https://github.com/swagger-api/swagger-ui.git
    
You can then open `swagger-ui/dist/index.html` in your preferred browser.    

<a name="#template-workflow"></a>
## Workflow: Changing template code
 First important rule: Changes to template classes should ALWAYS be done in the template repository. Otherwise, you are likely to get merge conflicts and strange results (like missing code) when pulling from the template.
 
 A simple workflow to enforce that rule:
 
 For each modification in your changeset check whether the modified file is contained in the template.
 If so, then copy&paste the change into the template and revert them in your repo. Commit and push the changes in the template. Pull changes down to your repo. Commit other changes into your repo. Done!
 
     $ git pull "template", master
     $ git commit
     $ git push "origin", master
 