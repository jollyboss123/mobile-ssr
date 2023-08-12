# Mobile server side rendering

### How to use
1. Create directory `Desktop/in` under your Home directory
2. Place json files with rendering details into `Desktop/in`, make sure files are versioned i.e. `xxx__v1.json`
3. Call from mobile GET `http://localhost:8081/get-flavor/{appId}`

### How it functions
1. User place json files for rendering in specified directory
2. Server will poll periodically & split json file into titles, color schemes & assets
3. Server then caches titles, color schemes & assets locally
4. User calls `getFlavor` API & retrieve from cache, if cache is empty retrieve from local directory

### Reference Documentation

* [A Deep Dive into Airbnb’s Server-Driven UI System](https://medium.com/airbnb-engineering/a-deep-dive-into-airbnbs-server-driven-ui-system-842244c5f5)

### Goals
- [X] support render of titles & strings
- [X] support render of color schemes 
- [X] support render of assets
- [ ] support render components by sections and pages
- [ ] support Graphql
