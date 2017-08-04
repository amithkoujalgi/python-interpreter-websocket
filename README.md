# Python Interactive Shell server over web

Connect and access your machine's Python interactive shell over a web interface.

Run the server:
```bash
mvn jetty:run
```

Fire up your browser and navigate to [http://localhost:8080](http://localhost:8080) and click on **Connect** button.

You should be able to see the following:
```
Connected!

Python 3.6.1 (default, Apr  4 2017, 09:40:21) 
[GCC 4.2.1 Compatible Apple LLVM 8.1.0 (clang-802.0.38)] on darwin
Type "help", "copyright", "credits" or "license" for more information.
>>>
```
You can now type in Python commands from a web interface.

And yes, you can have multiple instances of Python interpreters - just open another tab in the browser.
