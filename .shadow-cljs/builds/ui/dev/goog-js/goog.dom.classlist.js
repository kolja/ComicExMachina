["^ ","~:resource-id",["~:shadow.build.classpath/resource","goog/dom/classlist.js"],"~:js","goog.provide(\"goog.dom.classlist\");\ngoog.require(\"goog.array\");\ngoog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST = goog.define(\"goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST\", false);\ngoog.dom.classlist.getClassName_ = function(element) {\n  return typeof element.className == \"string\" ? element.className : element.getAttribute && element.getAttribute(\"class\") || \"\";\n};\ngoog.dom.classlist.get = function(element) {\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\n    return element.classList;\n  }\n  return goog.dom.classlist.getClassName_(element).match(/\\S+/g) || [];\n};\ngoog.dom.classlist.set = function(element, className) {\n  if (typeof element.className == \"string\") {\n    element.className = className;\n    return;\n  } else {\n    if (element.setAttribute) {\n      element.setAttribute(\"class\", className);\n    }\n  }\n};\ngoog.dom.classlist.contains = function(element, className) {\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\n    return element.classList.contains(className);\n  }\n  return goog.array.contains(goog.dom.classlist.get(element), className);\n};\ngoog.dom.classlist.add = function(element, className) {\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\n    element.classList.add(className);\n    return;\n  }\n  if (!goog.dom.classlist.contains(element, className)) {\n    var oldClassName = goog.dom.classlist.getClassName_(element);\n    goog.dom.classlist.set(element, oldClassName + (oldClassName.length > 0 ? \" \" + className : className));\n  }\n};\ngoog.dom.classlist.addAll = function(element, classesToAdd) {\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\n    goog.array.forEach(classesToAdd, function(className) {\n      goog.dom.classlist.add(element, className);\n    });\n    return;\n  }\n  var classMap = {};\n  goog.array.forEach(goog.dom.classlist.get(element), function(className) {\n    classMap[className] = true;\n  });\n  goog.array.forEach(classesToAdd, function(className) {\n    classMap[className] = true;\n  });\n  var newClassName = \"\";\n  for (var className in classMap) {\n    newClassName += newClassName.length > 0 ? \" \" + className : className;\n  }\n  goog.dom.classlist.set(element, newClassName);\n};\ngoog.dom.classlist.remove = function(element, className) {\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\n    element.classList.remove(className);\n    return;\n  }\n  if (goog.dom.classlist.contains(element, className)) {\n    goog.dom.classlist.set(element, goog.array.filter(goog.dom.classlist.get(element), function(c) {\n      return c != className;\n    }).join(\" \"));\n  }\n};\ngoog.dom.classlist.removeAll = function(element, classesToRemove) {\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\n    goog.array.forEach(classesToRemove, function(className) {\n      goog.dom.classlist.remove(element, className);\n    });\n    return;\n  }\n  goog.dom.classlist.set(element, goog.array.filter(goog.dom.classlist.get(element), function(className) {\n    return !goog.array.contains(classesToRemove, className);\n  }).join(\" \"));\n};\ngoog.dom.classlist.enable = function(element, className, enabled) {\n  if (enabled) {\n    goog.dom.classlist.add(element, className);\n  } else {\n    goog.dom.classlist.remove(element, className);\n  }\n};\ngoog.dom.classlist.enableAll = function(element, classesToEnable, enabled) {\n  var f = enabled ? goog.dom.classlist.addAll : goog.dom.classlist.removeAll;\n  f(element, classesToEnable);\n};\ngoog.dom.classlist.swap = function(element, fromClass, toClass) {\n  if (goog.dom.classlist.contains(element, fromClass)) {\n    goog.dom.classlist.remove(element, fromClass);\n    goog.dom.classlist.add(element, toClass);\n    return true;\n  }\n  return false;\n};\ngoog.dom.classlist.toggle = function(element, className) {\n  var add = !goog.dom.classlist.contains(element, className);\n  goog.dom.classlist.enable(element, className, add);\n  return add;\n};\ngoog.dom.classlist.addRemove = function(element, classToRemove, classToAdd) {\n  goog.dom.classlist.remove(element, classToRemove);\n  goog.dom.classlist.add(element, classToAdd);\n};\n","~:source","// Copyright 2012 The Closure Library Authors. All Rights Reserved.\n//\n// Licensed under the Apache License, Version 2.0 (the \"License\");\n// you may not use this file except in compliance with the License.\n// You may obtain a copy of the License at\n//\n//      http://www.apache.org/licenses/LICENSE-2.0\n//\n// Unless required by applicable law or agreed to in writing, software\n// distributed under the License is distributed on an \"AS-IS\" BASIS,\n// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n// See the License for the specific language governing permissions and\n// limitations under the License.\n\n/**\n * @fileoverview Utilities for detecting, adding and removing classes.  Prefer\n * this over goog.dom.classes for new code since it attempts to use classList\n * (DOMTokenList: http://dom.spec.whatwg.org/#domtokenlist) which is faster\n * and requires less code.\n *\n * Note: these utilities are meant to operate on HTMLElements and SVGElements\n * and may have unexpected behavior on elements with differing interfaces.\n */\n\n\ngoog.provide('goog.dom.classlist');\n\ngoog.require('goog.array');\n\n\n/**\n * Override this define at build-time if you know your target supports it.\n * @define {boolean} Whether to use the classList property (DOMTokenList).\n */\ngoog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST =\n    goog.define('goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST', false);\n\n\n/**\n * A wrapper which ensures correct functionality when interacting with\n * SVGElements\n * @param {?Element} element DOM node to get the class name of.\n * @return {string}\n * @private\n */\ngoog.dom.classlist.getClassName_ = function(element) {\n  // If className is an instance of SVGAnimatedString use getAttribute\n  return typeof element.className == 'string' ?\n      element.className :\n      element.getAttribute && element.getAttribute('class') || '';\n};\n\n\n/**\n * Gets an array-like object of class names on an element.\n * @param {Element} element DOM node to get the classes of.\n * @return {!IArrayLike<?>} Class names on `element`.\n */\ngoog.dom.classlist.get = function(element) {\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\n    return element.classList;\n  }\n\n  return goog.dom.classlist.getClassName_(element).match(/\\S+/g) || [];\n};\n\n\n/**\n * Sets the entire class name of an element.\n * @param {Element} element DOM node to set class of.\n * @param {string} className Class name(s) to apply to element.\n */\ngoog.dom.classlist.set = function(element, className) {\n  // If className is an instance of SVGAnimatedString use setAttribute\n  if ((typeof element.className) == 'string') {\n    element.className = className;\n    return;\n  } else if (element.setAttribute) {\n    element.setAttribute('class', className);\n  }\n};\n\n\n/**\n * Returns true if an element has a class.  This method may throw a DOM\n * exception for an invalid or empty class name if DOMTokenList is used.\n * @param {Element} element DOM node to test.\n * @param {string} className Class name to test for.\n * @return {boolean} Whether element has the class.\n */\ngoog.dom.classlist.contains = function(element, className) {\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\n    return element.classList.contains(className);\n  }\n  return goog.array.contains(goog.dom.classlist.get(element), className);\n};\n\n\n/**\n * Adds a class to an element.  Does not add multiples of class names.  This\n * method may throw a DOM exception for an invalid or empty class name if\n * DOMTokenList is used.\n * @param {Element} element DOM node to add class to.\n * @param {string} className Class name to add.\n */\ngoog.dom.classlist.add = function(element, className) {\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\n    element.classList.add(className);\n    return;\n  }\n\n  if (!goog.dom.classlist.contains(element, className)) {\n    // Ensure we add a space if this is not the first class name added.\n    var oldClassName = goog.dom.classlist.getClassName_(element);\n    goog.dom.classlist.set(\n        element,\n        oldClassName +\n            (oldClassName.length > 0 ? (' ' + className) : className));\n  }\n};\n\n\n/**\n * Convenience method to add a number of class names at once.\n * @param {Element} element The element to which to add classes.\n * @param {IArrayLike<string>} classesToAdd An array-like object\n * containing a collection of class names to add to the element.\n * This method may throw a DOM exception if classesToAdd contains invalid\n * or empty class names.\n */\ngoog.dom.classlist.addAll = function(element, classesToAdd) {\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\n    goog.array.forEach(classesToAdd, function(className) {\n      goog.dom.classlist.add(element, className);\n    });\n    return;\n  }\n\n  var classMap = {};\n\n  // Get all current class names into a map.\n  goog.array.forEach(goog.dom.classlist.get(element), function(className) {\n    classMap[className] = true;\n  });\n\n  // Add new class names to the map.\n  goog.array.forEach(\n      classesToAdd, function(className) { classMap[className] = true; });\n\n  // Flatten the keys of the map into the className.\n  var newClassName = '';\n  for (var className in classMap) {\n    newClassName += newClassName.length > 0 ? (' ' + className) : className;\n  }\n  goog.dom.classlist.set(element, newClassName);\n};\n\n\n/**\n * Removes a class from an element.  This method may throw a DOM exception\n * for an invalid or empty class name if DOMTokenList is used.\n * @param {Element} element DOM node to remove class from.\n * @param {string} className Class name to remove.\n */\ngoog.dom.classlist.remove = function(element, className) {\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\n    element.classList.remove(className);\n    return;\n  }\n\n  if (goog.dom.classlist.contains(element, className)) {\n    // Filter out the class name.\n    goog.dom.classlist.set(\n        element,\n        goog.array\n            .filter(\n                goog.dom.classlist.get(element),\n                function(c) {\n                  return c != className;\n                })\n            .join(' '));\n  }\n};\n\n\n/**\n * Removes a set of classes from an element.  Prefer this call to\n * repeatedly calling `goog.dom.classlist.remove` if you want to remove\n * a large set of class names at once.\n * @param {Element} element The element from which to remove classes.\n * @param {IArrayLike<string>} classesToRemove An array-like object\n * containing a collection of class names to remove from the element.\n * This method may throw a DOM exception if classesToRemove contains invalid\n * or empty class names.\n */\ngoog.dom.classlist.removeAll = function(element, classesToRemove) {\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\n    goog.array.forEach(classesToRemove, function(className) {\n      goog.dom.classlist.remove(element, className);\n    });\n    return;\n  }\n\n  // Filter out those classes in classesToRemove.\n  goog.dom.classlist.set(\n      element,\n      goog.array\n          .filter(\n              goog.dom.classlist.get(element),\n              function(className) {\n                // If this class is not one we are trying to remove,\n                // add it to the array of new class names.\n                return !goog.array.contains(classesToRemove, className);\n              })\n          .join(' '));\n};\n\n\n/**\n * Adds or removes a class depending on the enabled argument.  This method\n * may throw a DOM exception for an invalid or empty class name if DOMTokenList\n * is used.\n * @param {Element} element DOM node to add or remove the class on.\n * @param {string} className Class name to add or remove.\n * @param {boolean} enabled Whether to add or remove the class (true adds,\n *     false removes).\n */\ngoog.dom.classlist.enable = function(element, className, enabled) {\n  if (enabled) {\n    goog.dom.classlist.add(element, className);\n  } else {\n    goog.dom.classlist.remove(element, className);\n  }\n};\n\n\n/**\n * Adds or removes a set of classes depending on the enabled argument.  This\n * method may throw a DOM exception for an invalid or empty class name if\n * DOMTokenList is used.\n * @param {!Element} element DOM node to add or remove the class on.\n * @param {?IArrayLike<string>} classesToEnable An array-like object\n *     containing a collection of class names to add or remove from the element.\n * @param {boolean} enabled Whether to add or remove the classes (true adds,\n *     false removes).\n */\ngoog.dom.classlist.enableAll = function(element, classesToEnable, enabled) {\n  var f = enabled ? goog.dom.classlist.addAll : goog.dom.classlist.removeAll;\n  f(element, classesToEnable);\n};\n\n\n/**\n * Switches a class on an element from one to another without disturbing other\n * classes. If the fromClass isn't removed, the toClass won't be added.  This\n * method may throw a DOM exception if the class names are empty or invalid.\n * @param {Element} element DOM node to swap classes on.\n * @param {string} fromClass Class to remove.\n * @param {string} toClass Class to add.\n * @return {boolean} Whether classes were switched.\n */\ngoog.dom.classlist.swap = function(element, fromClass, toClass) {\n  if (goog.dom.classlist.contains(element, fromClass)) {\n    goog.dom.classlist.remove(element, fromClass);\n    goog.dom.classlist.add(element, toClass);\n    return true;\n  }\n  return false;\n};\n\n\n/**\n * Removes a class if an element has it, and adds it the element doesn't have\n * it.  Won't affect other classes on the node.  This method may throw a DOM\n * exception if the class name is empty or invalid.\n * @param {Element} element DOM node to toggle class on.\n * @param {string} className Class to toggle.\n * @return {boolean} True if class was added, false if it was removed\n *     (in other words, whether element has the class after this function has\n *     been called).\n */\ngoog.dom.classlist.toggle = function(element, className) {\n  var add = !goog.dom.classlist.contains(element, className);\n  goog.dom.classlist.enable(element, className, add);\n  return add;\n};\n\n\n/**\n * Adds and removes a class of an element.  Unlike\n * {@link goog.dom.classlist.swap}, this method adds the classToAdd regardless\n * of whether the classToRemove was present and had been removed.  This method\n * may throw a DOM exception if the class names are empty or invalid.\n *\n * @param {Element} element DOM node to swap classes on.\n * @param {string} classToRemove Class to remove.\n * @param {string} classToAdd Class to add.\n */\ngoog.dom.classlist.addRemove = function(element, classToRemove, classToAdd) {\n  goog.dom.classlist.remove(element, classToRemove);\n  goog.dom.classlist.add(element, classToAdd);\n};\n","~:compiled-at",1605343776608,"~:source-map-json","{\n\"version\":3,\n\"file\":\"goog.dom.classlist.js\",\n\"lineCount\":109,\n\"mappings\":\"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\",\n\"sources\":[\"goog/dom/classlist.js\"],\n\"sourcesContent\":[\"// Copyright 2012 The Closure Library Authors. All Rights Reserved.\\n//\\n// Licensed under the Apache License, Version 2.0 (the \\\"License\\\");\\n// you may not use this file except in compliance with the License.\\n// You may obtain a copy of the License at\\n//\\n//      http://www.apache.org/licenses/LICENSE-2.0\\n//\\n// Unless required by applicable law or agreed to in writing, software\\n// distributed under the License is distributed on an \\\"AS-IS\\\" BASIS,\\n// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\\n// See the License for the specific language governing permissions and\\n// limitations under the License.\\n\\n/**\\n * @fileoverview Utilities for detecting, adding and removing classes.  Prefer\\n * this over goog.dom.classes for new code since it attempts to use classList\\n * (DOMTokenList: http://dom.spec.whatwg.org/#domtokenlist) which is faster\\n * and requires less code.\\n *\\n * Note: these utilities are meant to operate on HTMLElements and SVGElements\\n * and may have unexpected behavior on elements with differing interfaces.\\n */\\n\\n\\ngoog.provide('goog.dom.classlist');\\n\\ngoog.require('goog.array');\\n\\n\\n/**\\n * Override this define at build-time if you know your target supports it.\\n * @define {boolean} Whether to use the classList property (DOMTokenList).\\n */\\ngoog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST =\\n    goog.define('goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST', false);\\n\\n\\n/**\\n * A wrapper which ensures correct functionality when interacting with\\n * SVGElements\\n * @param {?Element} element DOM node to get the class name of.\\n * @return {string}\\n * @private\\n */\\ngoog.dom.classlist.getClassName_ = function(element) {\\n  // If className is an instance of SVGAnimatedString use getAttribute\\n  return typeof element.className == 'string' ?\\n      element.className :\\n      element.getAttribute && element.getAttribute('class') || '';\\n};\\n\\n\\n/**\\n * Gets an array-like object of class names on an element.\\n * @param {Element} element DOM node to get the classes of.\\n * @return {!IArrayLike<?>} Class names on `element`.\\n */\\ngoog.dom.classlist.get = function(element) {\\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\\n    return element.classList;\\n  }\\n\\n  return goog.dom.classlist.getClassName_(element).match(/\\\\S+/g) || [];\\n};\\n\\n\\n/**\\n * Sets the entire class name of an element.\\n * @param {Element} element DOM node to set class of.\\n * @param {string} className Class name(s) to apply to element.\\n */\\ngoog.dom.classlist.set = function(element, className) {\\n  // If className is an instance of SVGAnimatedString use setAttribute\\n  if ((typeof element.className) == 'string') {\\n    element.className = className;\\n    return;\\n  } else if (element.setAttribute) {\\n    element.setAttribute('class', className);\\n  }\\n};\\n\\n\\n/**\\n * Returns true if an element has a class.  This method may throw a DOM\\n * exception for an invalid or empty class name if DOMTokenList is used.\\n * @param {Element} element DOM node to test.\\n * @param {string} className Class name to test for.\\n * @return {boolean} Whether element has the class.\\n */\\ngoog.dom.classlist.contains = function(element, className) {\\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\\n    return element.classList.contains(className);\\n  }\\n  return goog.array.contains(goog.dom.classlist.get(element), className);\\n};\\n\\n\\n/**\\n * Adds a class to an element.  Does not add multiples of class names.  This\\n * method may throw a DOM exception for an invalid or empty class name if\\n * DOMTokenList is used.\\n * @param {Element} element DOM node to add class to.\\n * @param {string} className Class name to add.\\n */\\ngoog.dom.classlist.add = function(element, className) {\\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\\n    element.classList.add(className);\\n    return;\\n  }\\n\\n  if (!goog.dom.classlist.contains(element, className)) {\\n    // Ensure we add a space if this is not the first class name added.\\n    var oldClassName = goog.dom.classlist.getClassName_(element);\\n    goog.dom.classlist.set(\\n        element,\\n        oldClassName +\\n            (oldClassName.length > 0 ? (' ' + className) : className));\\n  }\\n};\\n\\n\\n/**\\n * Convenience method to add a number of class names at once.\\n * @param {Element} element The element to which to add classes.\\n * @param {IArrayLike<string>} classesToAdd An array-like object\\n * containing a collection of class names to add to the element.\\n * This method may throw a DOM exception if classesToAdd contains invalid\\n * or empty class names.\\n */\\ngoog.dom.classlist.addAll = function(element, classesToAdd) {\\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\\n    goog.array.forEach(classesToAdd, function(className) {\\n      goog.dom.classlist.add(element, className);\\n    });\\n    return;\\n  }\\n\\n  var classMap = {};\\n\\n  // Get all current class names into a map.\\n  goog.array.forEach(goog.dom.classlist.get(element), function(className) {\\n    classMap[className] = true;\\n  });\\n\\n  // Add new class names to the map.\\n  goog.array.forEach(\\n      classesToAdd, function(className) { classMap[className] = true; });\\n\\n  // Flatten the keys of the map into the className.\\n  var newClassName = '';\\n  for (var className in classMap) {\\n    newClassName += newClassName.length > 0 ? (' ' + className) : className;\\n  }\\n  goog.dom.classlist.set(element, newClassName);\\n};\\n\\n\\n/**\\n * Removes a class from an element.  This method may throw a DOM exception\\n * for an invalid or empty class name if DOMTokenList is used.\\n * @param {Element} element DOM node to remove class from.\\n * @param {string} className Class name to remove.\\n */\\ngoog.dom.classlist.remove = function(element, className) {\\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\\n    element.classList.remove(className);\\n    return;\\n  }\\n\\n  if (goog.dom.classlist.contains(element, className)) {\\n    // Filter out the class name.\\n    goog.dom.classlist.set(\\n        element,\\n        goog.array\\n            .filter(\\n                goog.dom.classlist.get(element),\\n                function(c) {\\n                  return c != className;\\n                })\\n            .join(' '));\\n  }\\n};\\n\\n\\n/**\\n * Removes a set of classes from an element.  Prefer this call to\\n * repeatedly calling `goog.dom.classlist.remove` if you want to remove\\n * a large set of class names at once.\\n * @param {Element} element The element from which to remove classes.\\n * @param {IArrayLike<string>} classesToRemove An array-like object\\n * containing a collection of class names to remove from the element.\\n * This method may throw a DOM exception if classesToRemove contains invalid\\n * or empty class names.\\n */\\ngoog.dom.classlist.removeAll = function(element, classesToRemove) {\\n  if (goog.dom.classlist.ALWAYS_USE_DOM_TOKEN_LIST || element.classList) {\\n    goog.array.forEach(classesToRemove, function(className) {\\n      goog.dom.classlist.remove(element, className);\\n    });\\n    return;\\n  }\\n\\n  // Filter out those classes in classesToRemove.\\n  goog.dom.classlist.set(\\n      element,\\n      goog.array\\n          .filter(\\n              goog.dom.classlist.get(element),\\n              function(className) {\\n                // If this class is not one we are trying to remove,\\n                // add it to the array of new class names.\\n                return !goog.array.contains(classesToRemove, className);\\n              })\\n          .join(' '));\\n};\\n\\n\\n/**\\n * Adds or removes a class depending on the enabled argument.  This method\\n * may throw a DOM exception for an invalid or empty class name if DOMTokenList\\n * is used.\\n * @param {Element} element DOM node to add or remove the class on.\\n * @param {string} className Class name to add or remove.\\n * @param {boolean} enabled Whether to add or remove the class (true adds,\\n *     false removes).\\n */\\ngoog.dom.classlist.enable = function(element, className, enabled) {\\n  if (enabled) {\\n    goog.dom.classlist.add(element, className);\\n  } else {\\n    goog.dom.classlist.remove(element, className);\\n  }\\n};\\n\\n\\n/**\\n * Adds or removes a set of classes depending on the enabled argument.  This\\n * method may throw a DOM exception for an invalid or empty class name if\\n * DOMTokenList is used.\\n * @param {!Element} element DOM node to add or remove the class on.\\n * @param {?IArrayLike<string>} classesToEnable An array-like object\\n *     containing a collection of class names to add or remove from the element.\\n * @param {boolean} enabled Whether to add or remove the classes (true adds,\\n *     false removes).\\n */\\ngoog.dom.classlist.enableAll = function(element, classesToEnable, enabled) {\\n  var f = enabled ? goog.dom.classlist.addAll : goog.dom.classlist.removeAll;\\n  f(element, classesToEnable);\\n};\\n\\n\\n/**\\n * Switches a class on an element from one to another without disturbing other\\n * classes. If the fromClass isn't removed, the toClass won't be added.  This\\n * method may throw a DOM exception if the class names are empty or invalid.\\n * @param {Element} element DOM node to swap classes on.\\n * @param {string} fromClass Class to remove.\\n * @param {string} toClass Class to add.\\n * @return {boolean} Whether classes were switched.\\n */\\ngoog.dom.classlist.swap = function(element, fromClass, toClass) {\\n  if (goog.dom.classlist.contains(element, fromClass)) {\\n    goog.dom.classlist.remove(element, fromClass);\\n    goog.dom.classlist.add(element, toClass);\\n    return true;\\n  }\\n  return false;\\n};\\n\\n\\n/**\\n * Removes a class if an element has it, and adds it the element doesn't have\\n * it.  Won't affect other classes on the node.  This method may throw a DOM\\n * exception if the class name is empty or invalid.\\n * @param {Element} element DOM node to toggle class on.\\n * @param {string} className Class to toggle.\\n * @return {boolean} True if class was added, false if it was removed\\n *     (in other words, whether element has the class after this function has\\n *     been called).\\n */\\ngoog.dom.classlist.toggle = function(element, className) {\\n  var add = !goog.dom.classlist.contains(element, className);\\n  goog.dom.classlist.enable(element, className, add);\\n  return add;\\n};\\n\\n\\n/**\\n * Adds and removes a class of an element.  Unlike\\n * {@link goog.dom.classlist.swap}, this method adds the classToAdd regardless\\n * of whether the classToRemove was present and had been removed.  This method\\n * may throw a DOM exception if the class names are empty or invalid.\\n *\\n * @param {Element} element DOM node to swap classes on.\\n * @param {string} classToRemove Class to remove.\\n * @param {string} classToAdd Class to add.\\n */\\ngoog.dom.classlist.addRemove = function(element, classToRemove, classToAdd) {\\n  goog.dom.classlist.remove(element, classToRemove);\\n  goog.dom.classlist.add(element, classToAdd);\\n};\\n\"],\n\"names\":[\"goog\",\"provide\",\"require\",\"dom\",\"classlist\",\"ALWAYS_USE_DOM_TOKEN_LIST\",\"define\",\"getClassName_\",\"goog.dom.classlist.getClassName_\",\"element\",\"className\",\"getAttribute\",\"get\",\"goog.dom.classlist.get\",\"classList\",\"match\",\"set\",\"goog.dom.classlist.set\",\"setAttribute\",\"contains\",\"goog.dom.classlist.contains\",\"array\",\"add\",\"goog.dom.classlist.add\",\"oldClassName\",\"length\",\"addAll\",\"goog.dom.classlist.addAll\",\"classesToAdd\",\"forEach\",\"classMap\",\"newClassName\",\"remove\",\"goog.dom.classlist.remove\",\"filter\",\"c\",\"join\",\"removeAll\",\"goog.dom.classlist.removeAll\",\"classesToRemove\",\"enable\",\"goog.dom.classlist.enable\",\"enabled\",\"enableAll\",\"goog.dom.classlist.enableAll\",\"classesToEnable\",\"f\",\"swap\",\"goog.dom.classlist.swap\",\"fromClass\",\"toClass\",\"toggle\",\"goog.dom.classlist.toggle\",\"addRemove\",\"goog.dom.classlist.addRemove\",\"classToRemove\",\"classToAdd\"]\n}\n"]