# GPUP (Generic Platform for Utilizing Processes)
## Description
This project was made as part of a Software Development course in Java, taken in the 1st semester of 2022. In this project, I've created 2 desktop applications, for utilizing processes between managers and their workers.

## About GPUP
Generally, when an organization has a lot of tasks, there're dependencies between them, and utilizing it can be difficult. Furthermore, sometimes  
those tasks are executed by a group of people, and each one of them depended on others to finish their work. That's where GPUP comes in handy.\
With GPUP, all you have to do is create an XML file (according to a schema, of course) and fill the dependencies of the tasks, and GPUP will create a graph of the dependencies, manage the execution of the tasks, by dividing the tasks between the available workers and update their status in real-time, so you'll have the full image of the executed process.\
The same goes for compiling a project - the true purpose of GPUP existence.

## Functions in GPUP
### Manager
* Log in to the system, using a unique nickname
* Upload a new XML file to the server, available for all managers
* Get information about all graphs that existed in the system
* Download a graph from the server and check if the graph has any circles in it, and dependency between 2 tasks
* Create a request of executing only a few tasks in the graph and upload it to the server
* Choose a created request from the server and start, pause, and stop it at any time, while getting real-time information about its progress
### Worker
* Log in to the system, using a unique nickname
* Choose how many tasks to run in parallel
* Assign to any running request available in the system
* Get credits for each completed task
* See the history of executed tasks

## How it's done
   * Manager/Worker application: JavaFX
   * Server: Tomcat
   * Tests: Postman
   * 3rd libraries: OkHTTP, Gson
   * Technologies used: Multi-Threading
