# Simple Redirector Service
This module contains an implementation of a redirector that simply picks a random instance to serve a request. It contains
some simple logic to make sure it doesn't send repeated requests to the same instance within a short time frame.