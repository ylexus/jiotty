# Introduction
Jiotty is a library of java components aimed at home automation (IoT) enthusiasts who prefer to solder, assemble and code it all themselves as opposed to using 
one of existing automation hub solutions. This gives them 100% flexibility in all the automation tasks.
# Structure
Jiotty consists of a number of small independent modules that typically allow communication with a Thing, for example, Google Drive, 
a thermostat or a smart plug.
# Design principles
I am a fan of Google Guice as a dependency injection framework, a clearly visible point of entry to every API and good
encapsulation. A a result, an entry point to any component is the corresponding Guice module. 
If your application is not using Guice (wait but why?), use the following pattern to obtain an instance
 of any component:
```java
ExposedKeyModule<ComponentType> module = ComponentModule.builder()
    .setXxx()
    .build();
ComponentType component = Guice.createInjector(module).getInstance(module.getExposedKey());
```
  
Many components internally implement `LifecycleComponent` interface. Such components expect their `start()` method to be called on application startup and their `stop()` 
method to be called on application shutdown. A great way to manage that is to use the `Application` class that takes care of starting and stopping all the 
components. If you don't want to do that, you will have to perform this additional step on you application startup:
```java
module
    .findBindingsByType(new TypeLiteral<LifecycleComponent>() {})
    .stream()
    .map(binding -> binding.getProvider().get())
    .forEach(LifecycleComponent::start);
```

and then on application termination call `stop()` on the same components in reverse order. Refer to the source code of `Application` as an example.

# Quality
At the moment most of the code is used in my home automation scenarios, so I have good confidence in its quality. 
However, only a small part of the code is unit tested and none is documented. 
I am actively working on this.
        
# Components
## jiotty-appliance

## jiotty-connector-aws
An higher abstraction over Amazon IoT MQTT messaging.

TODO document them all
