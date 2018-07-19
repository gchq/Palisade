
<!---
Copyright 2018 Crown Copyright

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--->


# Koryphe Library

This library adds Koryphe Functions/Predicates to Palisade.

<img src="doc/img/koryphe_logo_text.png" width="200">

[Koryphe](https://github.com/gchq/koryphe) provides several lightweight 
reusable functions and predicates that can be chained together and applied
to generic tuple objects. Using this library allows you to define your
Palisade Policy rules in standard reuseable way without having to
create lots of similar java classes. It also provides a mechanism
for extracting fields from objects/tuples and passing them directly to
functions/predicates.

For examples of using Koryphe see the palisade examples.
