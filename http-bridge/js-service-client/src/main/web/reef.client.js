/*
 * Copyright 2011 Green Energy Corp.
 *
 * Licensed to Green Energy Corp (www.greenenergycorp.com) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. Green Energy
 * Corp licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
(function($) {

    $.reefClient = function(options) {

        // IE8 doesnt work without this flag, just set to true, if its not capable we'll fail anyways
        // http://graphicmaniacs.com/note/getting-a-cross-domain-json-with-jquery-in-internet-explorer-8-and-later/
        jQuery.support.cors = true;

        var clientObject = {};

        var displayError = function(msg) {
            if(settings.error_div !== undefined){
                settings.error_div.html("<div class=\"client_error\">" + msg + "</div>");
            }
            return msg;
        };

        var settings = $.extend({
            // address of the server to query
            'server': '',
            // error message takes a string describing the error
            'error_function': displayError,
            // div we want to replace the contents of (if using default displayError)
            'error_div': undefined,
            // username and password to start logging in with
            'auto_login' : {
                'name' : undefined,
                'password' : undefined
            },
            // list of services sets we want to load
            'service_lists' : ['core']

        }, options);

        var doAjax = function(options) {

            if (settings.authToken) {
                var authHeader = {
                    'REEF_AUTH_TOKEN': settings.authToken
                };
                if (options.headers) {
                    $.extend(authHeader, options.headers);
                }
                else {
                    options.headers = authHeader;
                }
            }

            // add the standard options
            var requestSettings = $.extend({
                type: 'GET',
                dataType: 'json',
                success: function(jsonData, textStatus, jqXhdr) {
                    if (jsonData) {
                        options.resultFuture.resolve(jsonData, jqXhdr);
                    } else {
                        options.resultFuture.reject("No data returned");
                    }

                    // kick off the next request (if theres any queued)
                    $(clientObject).dequeue();
                },
                error: function(XMLHttpRequest, textStatus, errorThrown) {
                    var msg = XMLHttpRequest.status + " " + XMLHttpRequest.statusText;
                    if (errorThrown) {
                        msg += " " + errorThrown;
                    }
                    if(XMLHttpRequest.status == 0) {
                        msg = "Couldn't connect to server.";
                    }
                    options.resultFuture.reject(msg);
                    // kick off the next request (if theres any queued)
                    $(clientObject).dequeue();
                }
            }, options);

            // start the asynchronous request
            $.ajax(requestSettings);
        };

        var enqueueRequest = function(options) {
            // use the jQuery queuing functions to maintain a "single threaded" queue of requests
            $(clientObject).queue(function() {
                doAjax(options);
            });
        };

        var apiRequest = function(options) {
            var future = $.Deferred()

            enqueueRequest({
                url: settings.server + "/api/" + options.request,
                data: options.data,
                resultFuture: future
            });

            var parsedPromise = future.pipe(function(jsonData, jqXhdr) {
                // TODO: expose headers support is coming
                //var style = jqXhdr.getResponseHeader("REEF_RETURN_STYLE");
                var style = options.style;
                switch (style) {
                case "MULTI": return jsonData.results;
                case "SINGLE": return jsonData;
                }
                throw "unknown return style";
            }, settings.error_function);

            // define a helper that will go and fetch the description for
            parsedPromise.describeResultType = function(){
                return describe(options.resultType);
            };
            return parsedPromise;
        };

        var login = function(userName, userPassword) {
            if (settings.authToken) {
                throw "Already logged in, logout first";
            }

            var future = $.Deferred();

            enqueueRequest({
                url: settings.server + "/login",
                data: {
                    name: userName,
                    password: userPassword
                },
                type: 'GET',
                dataType: 'text',
                resultFuture: future
            });
            return future.pipe(function(jsonData, jqXhdr) {
                // console.log("Logged in: " + userName);
                settings.authToken = jsonData;
                settings.userName = userName;
                return jsonData;
           }, settings.error_function);

        };

        var logout = function(userName, userPassword) {
            if (settings.authToken === undefined) {
                throw "Already logged in, logout first";
            }

            var future = $.Deferred();

            future.pipe(function(status){
                settings.authToken = undefined;
            }, settings.error_function);

            // TODO: javascript logout needs to delete authToken
            future.resolve('OK');

            return future;

        };

        var describe = function(typeName) {

            var future = $.Deferred();

            enqueueRequest({
                url: settings.server + "/convert/" + typeName,
                type: 'GET',
                dataType: 'json',
                resultFuture: future
            });
            return future.pipe(function(jsonData, jqXhdr) {
                return jsonData;
           }, settings.error_function);

        };

        var toString = function(){
            // if(settings.userName === undefined) return settings.server;
            // else return settings.userName + "@" + settings.server;
            return settings.server;
        };

        clientObject = {
            // base function for making apiRequests, used by the generated api bindings, returns promise with result
            'apiRequest': apiRequest,
            // login a user, two parameters userName and userPassword, returns promise containing authToken
            'login': login,
            // logout the user and delete the authToken, returns promise containing status flag
            'logout' : logout,
            // get the protobuf description of the type, takes object id, returns promise containing JSON formatted decriptor
            'describe' : describe,
            // returns string describing user and server we are using
            'toString' : toString
        };

        $.each(settings.service_lists, function(i, name){
          try{
            eval("$.reefServiceList_" + name + "(clientObject);");
          }catch(err){
            alert("Can't load reef service list: " + name + ". Did you include reef.client.core-services.js?")
          }
        });

        if(settings.auto_login && settings.auto_login.name){
            clientObject.login(options.auto_login.name, options.auto_login.password);
        }

        return clientObject;
    };
})(jQuery);