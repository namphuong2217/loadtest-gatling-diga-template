package fetchAllUsersDataScenario

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class FetchAllUsersDataSimulation extends Simulation {

  // Gatling Script Breakdown
  // 1. Protocol Setup
  val domain = "DOMAIN TO FETCH ALL USERS DATA";

  val httpProtocol = http
    .baseUrl("https://" + domain)

  def numberOfUsers: Int = 10
  def numberOfRequests: Int = 30
  def durationOfTest: Int = 60

  // query Keycloak login data. Always the same if only one user exists in csv file
  val csvFeederLoginDetails = csv("data/loginDetails.csv").random

  before {
    println("Begin Load Test Scenario: Fetch All Users Data")
    println("Running test with ${userCount} users")
    println("Running test with ${numberOfRequests} requests")
    println("Total test duration: ${testDuration} seconds")
  }

  after{
    println("End Load Test Scenario: Fetch All Users Data")
  }

  // Gatling Script Breakdown
  // 2. Scenario Definition

  // manage current user session
  val initSession = exec(flushCookieJar)
    .exec(session => session.set("userLoggedIn", false))

  val fetchAllUsersScenario = scenario("FetchAllUsersDataSimulation")
      .exec(initSession)
      // extract Keycloak user login data
      .feed(csvFeederLoginDetails)
      // check Login Page loads successfully
      .exec(
        http("Load Login Page")
          .get("/login")
          .check(status.is(200))
      )
      // check if Login succeeds
      .exec(
        http("Customer Login Action")
          .post("/login")
          .formParam("username", "${username}") // username variable comes from loginDetails.csv
          .formParam("password", "${password}") // password variable comes from loginDetails.csv
          .check(status.is(200))
      )
      // set current user login status to true
      .exec(session => session.set("customerLoggedIn", true))
      .exec(http("Fetch All Users Data")
        .get("/all-users")
        .check(status.is(200)))

  // Gatling Script Breakdown
  // 3. Load Simulation Design

  //  Injects users at numberOfUsers rate per second
  //  , during durationOfTest duration. Users will be injected at regular intervals.
  setUp(fetchAllUsersScenario.inject(
    constantUsersPerSec(numberOfUsers) during (durationOfTest.seconds)))
    .protocols(httpProtocol)
}
