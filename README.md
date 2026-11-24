# Flight Services API

A simple REST API for managing **flights**, **passengers**, and **reservations** (Spring Boot + MySQL).
This guide walks you through running it on an AWS EC2 instance and testing every endpoint.

---

## How this works (read this first)

You will use **two terminals** on your own computer:

| Terminal | What it's for | Where it runs commands |
|---|---|---|
| **Terminal 1 – Server** | SSH into EC2, set up the database, start the app. **Leave it open** – the app runs here. | On the EC2 instance |
| **Terminal 2 – Client** | Send GET / POST / PUT / DELETE requests to the app. | On your own computer (PowerShell) |

> If you close Terminal 1 while the app runs in the foreground, the app stops.
> See [Keep the server running after you log out](#keep-the-server-running-after-you-log-out) to avoid that.

Values used throughout this guide – replace them with your own:

| Placeholder | Example | Where to find it |
|---|---|---|
| `C:\path\to\my-key.pem` | `C:\Users\you\Downloads\my-key.pem` | The key pair file you downloaded when creating the EC2 instance |
| `YOUR-EC2-PUBLIC-DNS` | `ec2-1-2-3-4.eu-north-1.compute.amazonaws.com` | EC2 console → your instance → **Public IPv4 DNS** (it changes if the instance is stopped and started) |
| `ec2-user` | `ec2-user` | Login user for Amazon Linux (`ubuntu` on Ubuntu instances) |

---

## 1. One-time AWS setup

In the EC2 console → your instance → **Security** tab → click the security group → **Edit inbound rules** and make sure you have:

| Type | Port | Source |
|---|---|---|
| SSH | 22 | My IP |
| Custom TCP | 8080 | My IP (or `0.0.0.0/0` for anyone) |

Without the 8080 rule your requests will time out.

---

## 2. Build the JAR and copy files to EC2

Do this in **Terminal 2 (your computer)**, inside the project folder.

```powershell
cd C:\path\to\flightservices

# Build (creates target\flightservices.jar)
mvn clean package
```

Copy the JAR and the database script to the EC2 instance:

```powershell
scp -i "C:\path\to\my-key.pem" target\flightservices.jar flightdb.sql ec2-user@YOUR-EC2-PUBLIC-DNS:~
```

**Alternative – via S3:** upload from your computer, then download on EC2 (EC2 needs an IAM role with S3 read access):

```powershell
# Your computer
aws s3 cp target\flightservices.jar s3://YOUR-BUCKET/flightservices/flightservices.jar
aws s3 cp flightdb.sql s3://YOUR-BUCKET/flightservices/flightdb.sql
```
```bash
# On EC2 (Terminal 1, after step 3)
aws s3 cp s3://YOUR-BUCKET/flightservices/flightservices.jar ~/
aws s3 cp s3://YOUR-BUCKET/flightservices/flightdb.sql ~/
```

---

## 3. SSH into the EC2 instance (Terminal 1)

Open a **new** PowerShell window – this is **Terminal 1**:

```powershell
ssh -i "C:\path\to\my-key.pem" ec2-user@YOUR-EC2-PUBLIC-DNS
```

Type `yes` if asked to trust the host. Your prompt changes to something like `[ec2-user@ip-172-31-... ~]$` – you are now on the server.

<details>
<summary>Error: "UNPROTECTED PRIVATE KEY FILE" / "bad permissions"</summary>

Windows needs the key to be readable only by you. Run this once in PowerShell, then try `ssh` again:

```powershell
icacls "C:\path\to\my-key.pem" /inheritance:r
icacls "C:\path\to\my-key.pem" /grant:r "$($env:USERNAME):(R)"
```
</details>

---

## 4. Install Java and MySQL (first time only)

Still in **Terminal 1 (on EC2)**. Skip this section if you have already done it.

**Amazon Linux 2023** (the default for new instances):

```bash
sudo dnf install -y java-17-amazon-corretto-headless mariadb105-server
sudo systemctl enable --now mariadb
```

**Amazon Linux 2** (older instances):

```bash
sudo yum install -y java-17-amazon-corretto-headless mariadb-server
sudo systemctl enable --now mariadb
```

Check both are working:

```bash
java -version
sudo systemctl status mariadb --no-pager     # should say "active (running)"
```

> MariaDB is a drop-in replacement for MySQL – the app works with either.

---

## 5. Create the database, tables, and sample data

Still in **Terminal 1 (on EC2)**, in the folder where you copied `flightdb.sql`:

```bash
# Creates the "reservation" database, the flight/passenger/reservation tables,
# and loads sample data (10 flights, 6 passengers, 6 reservations)
sudo mysql < flightdb.sql
```

Create a database user for the app. **Pick your own password** and use it instead of `YourStrongPassword` everywhere below:

```bash
sudo mysql -e "CREATE USER IF NOT EXISTS 'flightapp'@'localhost' IDENTIFIED BY 'YourStrongPassword';
GRANT ALL PRIVILEGES ON reservation.* TO 'flightapp'@'localhost';
FLUSH PRIVILEGES;"
```

Check the data loaded:

```bash
sudo mysql -e "USE reservation; SELECT COUNT(*) FROM flight; SELECT COUNT(*) FROM passenger; SELECT COUNT(*) FROM reservation;"
```

You should see `10`, `6`, and `6`.

> **Starting over:** `flightdb.sql` fails if the tables already exist. To wipe everything and reload:
> `sudo mysql -e "DROP DATABASE reservation;"` then run `sudo mysql < flightdb.sql` again.

---

## 6. Start the server (Terminal 1)

The app needs the database password. **It is never stored in the code** – give it to the app in one of two ways.

**Option A – environment variables on the command line** (quickest):

```bash
DB_USERNAME=flightapp DB_PASSWORD='YourStrongPassword' java -jar flightservices.jar
```

**Option B – a private settings file** (so you don't retype it). In the same folder as the JAR:

```bash
nano application-local.properties
```

Type these two lines (with your password), then save with `Ctrl + O`, `Enter`, and exit with `Ctrl + X`:

```properties
spring.datasource.username=flightapp
spring.datasource.password=YourStrongPassword
```

Lock the file down and start the app:

```bash
chmod 600 application-local.properties
java -jar flightservices.jar
```

The app automatically reads `application-local.properties` from the folder you start it in.
That file is in `.gitignore`, so it is never committed (`application-local.properties.example` shows the format).

Wait until you see a line like:

```
Started FlightservicesApplication in 6.5 seconds
```

**The server is now running. Leave Terminal 1 open** and switch to Terminal 2.
To stop the server later, press `Ctrl + C` in Terminal 1.

Optional settings (put them before `java -jar` like the ones above):

| Variable | Default | Meaning |
|---|---|---|
| `DB_HOST` | `localhost` | Database server (e.g. an RDS endpoint) |
| `DB_PORT` | `3306` | Database port |
| `DB_NAME` | `reservation` | Database name |
| `DB_USERNAME` | `root` | Database user |
| `DB_PASSWORD` | **none – required** | Database password |
| `PORT` | `8080` | Port the API listens on |
| `SHOW_SQL` | `false` | `true` prints every SQL query |

---

## 7. Send requests (Terminal 2)

Open a **new** PowerShell window on your computer – this is **Terminal 2**.
Every request in this section is typed here, **not** in the SSH window.

First, save the server address in a variable (do this every time you open a new Terminal 2):

```powershell
$base = "http://YOUR-EC2-PUBLIC-DNS:8080/flightservices"
```

Check the server is up:

```powershell
Invoke-RestMethod "$base/actuator/health"
```

Expected: `status : UP`

> **Why `Invoke-RestMethod` and not `curl`?** It is built into PowerShell and handles JSON
> correctly. Windows PowerShell's `curl.exe` often breaks JSON quotes. GET requests also
> work by pasting the URL into a browser.

### Endpoint summary

All URLs start with `http://<EC2 address>:8080/flightservices`.

| Action | Method | URL | Body needed? |
|---|---|---|---|
| List all flights | GET | `/flights` | No |
| Search flights | GET | `/flights?from=AUS&to=NYC&departureDate=2018-02-05` | No |
| Get one flight | GET | `/flights/{id}` | No |
| Create a flight | POST | `/flights` | Yes |
| Update a flight | PUT | `/flights/{id}` | Yes (all fields) |
| Delete a flight | DELETE | `/flights/{id}` | No |
| List all passengers | GET | `/passengers` | No |
| Get one passenger | GET | `/passengers/{id}` | No |
| Create a passenger | POST | `/passengers` | Yes |
| Update a passenger | PUT | `/passengers/{id}` | Yes (all fields) |
| Delete a passenger | DELETE | `/passengers/{id}` | No (also deletes their reservations) |
| List all reservations | GET | `/reservations` | No |
| Get one reservation | GET | `/reservations/{id}` | No |
| Book a reservation | POST | `/reservations` | Yes (creates the passenger too) |
| Check in / change bags | PUT | `/reservations/{id}` | Yes (only the fields you want to change) |
| Delete a reservation | DELETE | `/reservations/{id}` | No |

> **IDs:** the sample data has flights 1–10, passengers 1–6, reservations 1–6.
> Anything you create gets the next free ID – **always use the `id` shown in the response**.

---

### Flights

**GET – list all flights**

```powershell
Invoke-RestMethod "$base/flights"
```

**GET – one flight**

```powershell
Invoke-RestMethod "$base/flights/1"
```

**GET – search** (all three parameters are required; date format `yyyy-MM-dd`)

```powershell
Invoke-RestMethod "$base/flights?from=AUS&to=NYC&departureDate=2018-02-05"
```

**POST – create a flight**

```powershell
$flight = @{
    flightNumber           = "DL100"
    operatingAirlines      = "Delta"
    departureCity          = "ATL"
    arrivalCity            = "LAX"
    dateOfDeparture        = "2026-12-01"
    estimatedDepartureTime = "2026-12-01T09:30:00"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$base/flights" -ContentType "application/json" -Body $flight
```

Note the `id` in the response (e.g. `11`) – the next examples use it.

**PUT – update a flight** (send **every** field, not just the changed ones)

```powershell
$flight = @{
    flightNumber           = "DL100"
    operatingAirlines      = "Delta"
    departureCity          = "ATL"
    arrivalCity            = "SFO"
    dateOfDeparture        = "2026-12-02"
    estimatedDepartureTime = "2026-12-02T14:00:00"
} | ConvertTo-Json

Invoke-RestMethod -Method Put -Uri "$base/flights/11" -ContentType "application/json" -Body $flight
```

**DELETE – delete a flight**

```powershell
Invoke-RestMethod -Method Delete -Uri "$base/flights/11"
```

No output means it worked. A flight that still has reservations (e.g. flight `1`) can't be deleted – you'll get **409 Conflict**. Delete its reservations first.

---

### Passengers

**GET – list all passengers**

```powershell
Invoke-RestMethod "$base/passengers"
```

**GET – one passenger**

```powershell
Invoke-RestMethod "$base/passengers/1"
```

**POST – create a passenger**

```powershell
$passenger = @{
    firstName  = "Lerato"
    middleName = "Grace"
    lastName   = "Mokoena"
    email      = "lerato.mokoena@example.com"
    phone      = "0825550199"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$base/passengers" -ContentType "application/json" -Body $passenger
```

Rules: `firstName` and `lastName` are required, `email` must be a valid email, `phone` is max 10 characters.

**PUT – update a passenger** (send every field; use the `id` from the POST response, e.g. `7`)

```powershell
$passenger = @{
    firstName  = "Lerato"
    middleName = "Grace"
    lastName   = "Dlamini"
    email      = "lerato.dlamini@example.com"
    phone      = "0825550199"
} | ConvertTo-Json

Invoke-RestMethod -Method Put -Uri "$base/passengers/7" -ContentType "application/json" -Body $passenger
```

**DELETE – delete a passenger** (also deletes their reservations)

```powershell
Invoke-RestMethod -Method Delete -Uri "$base/passengers/7"
```

---

### Reservations

**GET – list all reservations**

```powershell
Invoke-RestMethod "$base/reservations"
```

**GET – one reservation** (includes the full flight and passenger details)

```powershell
Invoke-RestMethod "$base/reservations/1" | ConvertTo-Json -Depth 5
```

`ConvertTo-Json -Depth 5` shows the nested flight and passenger instead of `@{...}`.

**POST – book a reservation** (creates a new passenger and a reservation on an existing flight)

```powershell
$booking = @{
    flightId           = 2
    passengerFirstName = "Sipho"
    passengerLastName  = "Ndlovu"
    passengerEmail     = "sipho.ndlovu@example.com"
    passengerPhone     = "0835550123"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$base/reservations" -ContentType "application/json" -Body $booking
```

The new reservation starts with `checkedIn = False` and `numberOfBags = 0`. Note its `id` (e.g. `7`).

**PUT – check in and set bags** (send only what you want to change)

```powershell
$update = @{ checkedIn = $true; numberOfBags = 2 } | ConvertTo-Json

Invoke-RestMethod -Method Put -Uri "$base/reservations/7" -ContentType "application/json" -Body $update
```

Change just the bags (check-in status stays as it is):

```powershell
Invoke-RestMethod -Method Put -Uri "$base/reservations/7" -ContentType "application/json" -Body '{"numberOfBags": 3}'
```

**DELETE – cancel a reservation**

```powershell
Invoke-RestMethod -Method Delete -Uri "$base/reservations/7"
```

---

### Using curl instead (Mac / Linux / Git Bash)

```bash
BASE=http://YOUR-EC2-PUBLIC-DNS:8080/flightservices

curl $BASE/flights
curl -X POST $BASE/reservations -H "Content-Type: application/json" \
  -d '{"flightId":2,"passengerFirstName":"Sipho","passengerLastName":"Ndlovu"}'
curl -X PUT $BASE/reservations/7 -H "Content-Type: application/json" -d '{"checkedIn":true,"numberOfBags":2}'
curl -X DELETE $BASE/reservations/7
```

---

## What the responses mean

| Status | Meaning | What to do |
|---|---|---|
| 200 OK | Success (GET / PUT) | – |
| 201 Created | Record created (POST) | Note the `id` in the response |
| 204 No Content | Deleted (DELETE) | Nothing is printed – that's normal |
| 400 Bad Request | Missing / invalid field or bad JSON | Read the `message` – it names the field |
| 404 Not Found | That ID doesn't exist | List the records with GET to find valid IDs |
| 409 Conflict | Not allowed with current data (e.g. deleting a flight with reservations) | Delete the related records first |

In PowerShell, errors appear in red; the server's explanation is in the `message` part.

---

## Keep the server running after you log out

Instead of step 6, start the app in the background (Terminal 1):

```bash
DB_USERNAME=flightapp DB_PASSWORD='YourStrongPassword' nohup java -jar flightservices.jar > app.log 2>&1 &
tail -f app.log          # watch the log; Ctrl + C stops watching (the app keeps running)
```

You can now close Terminal 1. Later, to stop the app:

```bash
pkill -f flightservices.jar
```

---

## Deploying a new version

You only need a new JAR when the **code** changes. Adding data (POST / PUT / DELETE) does **not** need a new JAR.

1. Terminal 2: `mvn clean package`, then copy the new JAR to EC2 (step 2).
2. Terminal 1: stop the running app (`Ctrl + C` or `pkill -f flightservices.jar`).
3. Terminal 1: start it again (step 6).

---

## Troubleshooting

| Problem | Likely cause | Fix |
|---|---|---|
| `Connection refused` | The app isn't running on EC2 | Start it (step 6). Check with `ps aux \| grep flightservices` on EC2 |
| Request times out | Port 8080 not open | Add the inbound rule (step 1) |
| App exits with `Communications link failure` | Database not running / wrong host | `sudo systemctl start mariadb`; check `DB_HOST` |
| App exits with `Access denied for user` | Wrong DB username or password | Use the user/password from step 5 |
| `Table 'reservation.flight' doesn't exist` | Tables not created | Run step 5 |
| `ssh: Connection timed out` | Wrong address or port 22 closed | Use the current Public IPv4 DNS from the EC2 console; check the SSH rule |
| `curl: Failed to open booking.json` | The file isn't in the current folder | Use the `Invoke-RestMethod` examples above |
| URL starts with `http://http://` | Copy-paste typo | Use a single `http://` |

---

## Running locally (optional)

With MySQL running on your computer and `flightdb.sql` loaded:

```powershell
# One-time: create your private settings file, then put your MySQL username/password in it
Copy-Item application-local.properties.example application-local.properties
notepad application-local.properties

mvn spring-boot:run
# or
java -jar target\flightservices.jar
```

Then use `$base = "http://localhost:8080/flightservices"` in Terminal 2.

---

## Security notes

- **Never commit passwords, keys, or server addresses.** `.gitignore` already excludes
  `application-local.properties`, `.env` files, `*.pem` / `*.key` / `*.ppk` key files, and `*.log` files.
- Keep your EC2 key (`.pem`) outside the project folder.
- Restrict the security group's port 22 rule to **My IP**.
- If a password is ever pushed by mistake, **change the password** – deleting the file afterwards
  does not remove it from git history.
