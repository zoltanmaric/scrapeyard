# scrapeyard
A simple REST API for scheduling asynchronous batches of flight searches

The API accepts JSON requests containing the following information:

* a set of viable departure airports
* a set of viable destination airports
* a range of viable departure dates
* a range of viable return dates

Here's an example request:

```json
{
 "email": "zoltanmaric@github.com",
 "criteria": {
  "origs": ["ZAG", "BUD"],
  "dests": ["DPS"],
  "depFrom": "2015-10-15",
  "depUntil": "2015-10-16",
  "retFrom": "2015-11-15",
  "retUntil": "2015-11-26"
 }
}
```

The server schedules a search and sends the user an e-mail containing all the search results.

The search is performed by scraping various flight search engine sites. Currently it scrapes the following sites:

* [Momondo](http://momondo.com)
* [Qatar Airways](http://qatarairway.com)
* [AIR.HR](http://air.hr)
