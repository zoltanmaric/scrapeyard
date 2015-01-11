# scrapeyard
### A simple REST API for scheduling batches of flight searches.

1. Set your search for **more than one** departure and destination airport
2. Give it **a range of departure and return dates**
3. Let it tumble through all the combinations for you, on multiple search engines
4. Get notified of the results via email

## Usage

The API accepts JSON POST requests on `/search` containing the following information:

* an email address
* a set of suitable departure airports
* a set of suitable destination airports
* a range of suitable departure dates
* a range of suitable return dates

Here's an example request:

```json
{
 "email": "zoltanmaric@github.com",
 "criteria": {
  "origs": ["ZAG", "BUD"],
  "dests": ["DPS"],
  "depFrom": "2015-07-20",
  "depUntil": "2015-07-20",
  "retFrom": "2015-07-29",
  "retUntil": "2015-07-30"
 }
}
```

which can be interpreted as

* Depart from Zagreb **or** Budapest
* Arrive in Denpasar (Bali)
* Depart on Jul 20, 2015
* Return between Jul 29, 2015 and Jul 30, 2015
* When done, send search results to `zoltanmaric@github.com`

The server schedules one search for each combination of departure and destination airports, and departure and return dates; and sends the user an e-mail containing all the search results.
The following is the body of an example result email:

```json
[{
  "params": {
    "origin": "ZAG",
    "destination": "DPS",
    "departure": "2015-07-20",
    "returning": "2015-07-29"
  },
  "price": "7173 HRK",
  "url": "http://avio.air.hr/airhr/ZAG/DPS/20.07.2015-29.07.2015/1/0/0/rt"
}, {
  "params": {
    "origin": "ZAG",
    "destination": "DPS",
    "departure": "2015-07-20",
    "returning": "2015-07-30"
  },
  "price": "7173 HRK",
  "url": "http://avio.air.hr/airhr/ZAG/DPS/20.07.2015-30.07.2015/1/0/0/rt"
}, {
  "params": {
    "origin": "BUD",
    "destination": "DPS",
    "departure": "2015-07-20",
    "returning": "2015-07-29"
  },
  "price": "6804 HRK",
  "url": "http://avio.air.hr/airhr/BUD/DPS/20.07.2015-29.07.2015/1/0/0/rt"
}, {
  "params": {
    "origin": "BUD",
    "destination": "DPS",
    "departure": "2015-07-20",
    "returning": "2015-07-30"
  },
  "price": "6942 HRK",
  "url": "http://avio.air.hr/airhr/BUD/DPS/20.07.2015-30.07.2015/1/0/0/rt"
}]
```

The search is performed by scraping various flight search engine sites. Currently it scrapes the following sites:

* [Momondo](http://momondo.com)
* [Qatar Airways](http://qatarairways.com)
* [AIR.HR](http://air.hr)

## Prerequisites

The server implementation currently assumes that a postfix SMTP server is available on `localhost`. To install such
an SMTP server on Ubuntu, complete the following steps:

* Install the SMTP server by running `sudo apt-get install postfix`
* After entering the above command it would ask for the basic settings as follows:
 * `General type of mail configuration:  Internet site`
 * `system mail name: "your_system_name"`
 * `Root and postmaster mail recipient: "leave it blank"`

Instructions taken from [this link](http://www.mindfiresolutions.com/Configure-SMTP-For-Localhost-In-Ubuntu-2421.php).
