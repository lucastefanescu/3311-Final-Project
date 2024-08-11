This project implements the backend for a service that computes the shortest path between Kevin Bacon and a given actor using shared movies. The solution uses Neo4j as the database management system and Java to query the database.
## Technologies
  -Java
  -Neo4j
  -Maven
  
## API Endpoints

  ## PUT Requests
  - /api/v1/addActor
    
    - Description: Adds an actor node into the database.
    - Body Parameters: name (String), actorId (String)
    - Responses:
      - 200 OK - Successful add
      - 400 BAD REQUEST - Bad request or actor already exists
      - 500 INTERNAL SERVER ERROR - Unsuccessful operation
    
  - /api/v1/addMovie
    
    - Description: Adds a movie node into the database.
    - Body Parameters: name (String), movieId (String)
    - Responses:
      - 200 OK - Successful add
      - 400 BAD REQUEST - Bad request or movie already exists
      - 500 INTERNAL SERVER ERROR - Unsuccessful operation
    
  - /api/v1/addRelationship
    
    - Description: Adds an ACTED_IN relationship between an actor and a movie.
    - Body Parameters: actorId (String), movieId (String)
    - Responses:
      - 200 OK - Successful add
      - 400 BAD REQUEST - Bad request or relationship already exists
      - 404 NOT FOUND - Actor or movie not found
      - 500 INTERNAL SERVER ERROR - Unsuccessful operation
    
  ## GET Requests
  - /api/v1/getActor
    
    - Description: Checks if an actor exists in the database.
    - Body Parameters: actorId (String)
    - Responses:
      - 200 OK - Actor found
      - 400 BAD REQUEST - Bad request
      - 404 NOT FOUND - Actor not found
      - 500 INTERNAL SERVER ERROR - Unsuccessful operation
  
  - /api/v1/getMovie
    
      - Description: Checks if a movie exists in the database.
      - Body Parameters: movieId (String)
      - Responses:
        - 200 OK - Movie found
        - 400 BAD REQUEST - Bad request
        - 404 NOT FOUND - Movie not found
        - 500 INTERNAL SERVER ERROR - Unsuccessful operation
    
  - /api/v1/hasRelationship
    
       - Description: Checks if there is a relationship between an actor and a movie.
       - Body Parameters: movieId (String), actorId (String)
       - Responses:
         - 200 OK - Relationship found
         - 400 BAD REQUEST - Bad request
         - 404 NOT FOUND - Actor or movie not found
         - 500 INTERNAL SERVER ERROR - Unsuccessful operation
    
  - /api/v1/computeBaconNumber
    
       - Description: Checks the Bacon number of an actor.
       - Body Parameters: actorId (String)
       - Responses:
         - 200 OK - Bacon number found
         - 400 BAD REQUEST - Bad request
         - 404 NOT FOUND - Actor or path not found
         - 500 INTERNAL SERVER ERROR - Unsuccessful operation
    
  - /api/v1/computeBaconPath
    
    - Description: Returns the shortest Bacon Path from the specified actor to Kevin Bacon.
    - Body Parameters: actorId (String)
    - Responses:
      - 200 OK - Path found
      - 400 BAD REQUEST - Bad request
      - 404 NOT FOUND - Actor or path not found
      - 500 INTERNAL SERVER ERROR - Unsuccessful operation
  
