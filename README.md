# Trevin JSON parser
## Overview
Trevin is a JSON parser based in Jackson FasterXML. It extends the base functionality in order to deal with models where exist circular references that would cause and infinite loop in the generated JSON representation of the model.
It dynamically generates reference objects to the original object in the model. The attributes to map in order to generate right references can be added in the code by using Java annotations.
Trevin JSON is also able to read back the JSON rebuilding all the original model relationships by adding dynamic proxies.
