# World Clock HTTP Server

A custom HTTP server built from scratch in Java using raw sockets. It serves a live world clock through any standard web browser, with no web frameworks or external libraries used.

## How to Run

**1. Compile the server:**
```bash
javac worldClock.java
```

**2. Start the server:**
```bash
java worldClock
```
The server will print: `World Clock Server is running on port 8888`

**3. Open your browser and navigate to:**
```
http://127.0.0.1:8888
```
> If running on WSL2, first run `hostname -I` and use the first IP address instead of `127.0.0.1`, e.g. `http://172.25.10.96:8888`

**4. To stop the server:**
```
Ctrl + C
```


## Features

### Core Functionality
- Displays the current South African (Johannesburg) time, updating every second
- Click any city name to view that city's current time alongside SA time
- Supported cities: London, New York, Los Angeles, Tokyo, Sydney, Dubai, Beijing, Moscow, Paris, Cairo

### Bonus Features

#### UTC Offset Display
Each city's clock box shows its UTC offset (e.g. `UTC+9:00` for Tokyo, `UTC-5:00` for New York). This is calculated directly from the timezone data at runtime, so it automatically accounts for daylight saving time changes.

#### Time Difference from South Africa
When a city is selected, a message below its clock shows how many hours ahead or behind it is relative to South Africa, for example:
- `7 hours ahead of South Africa` (Tokyo)
- `2 hours behind South Africa` (London)
- `Same time as South Africa` (Cairo)

#### HTTP RFC Compliance
The server demonstrates understanding of the HTTP/1.1 specification (RFC 2616) through:

| Header | Purpose |
|---|---|
| `Date` | RFC-required timestamp on every response |
| `Content-Type: text/html; charset=UTF-8` | Declares content type and encoding |
| `Content-Length` | Exact byte length of the response body |
| `Connection: close` | Signals the connection will close after response |
| `Cache-Control: no-store` | Prevents browser from caching the live clock |
| `Server: WorldClockServer/1.0` | Identifies the server software |

#### Proper HTTP Status Codes
- `200 OK` — returned for valid requests
- `404 Not Found` — returned for unknown paths (e.g. `/randompage`) with a styled error page and a back link

#### HEAD Request Support
The server correctly handles `HEAD` requests as required by the HTTP spec — returning all response headers with no body.


## Testing

**Test raw HTTP response headers:**
```bash
curl -v http://127.0.0.1:8888

curl -v http://172.25.10.96:8888
```

**Test 404 handling:**
```bash
curl -v http://127.0.0.1:8888/randompage

curl -v http://172.25.10.96:8888/randompage
```

**Test HEAD request:**
```bash
curl -v --head http://127.0.0.1:8888

curl -v --head http://172.25.10.96:8888
```