# CRTM

A research on how Metro de Madrid NFC cards works.

- [NFC Card info](#nfc-card-info)
	- [Card type](#card-type)
	- [Application list](#application-list)
	- [Files](#files)
- [Communication](#communication)
	- [Init connection](#init-connection)
	- [Check balance](#check-balance)
	- [Constants](#constants)
	- [PDU status values](#pdu-status-values)
- [App](#app)
	- [Constants](#constants)
	- [Network status values](#network-status-values)
- [Server](#server)
	- [Status codes](#status-codes)
	- [Endpoints](#endpoints) 
- [Security researchs](#security-researchs)
	- [Side channel attacks](#side-channel-attacks)
- [Custom APK for debugging NFC communication](#custom-apk-for-debugging-nfc-communication)

## NFC Card info

### Card type

- Mifare DESfire EV1 (MF3ICD41 [1]) [2] 

### Application list

Each application can have up to 14 keys.


- AID: `0x00` (PICC). 1 access key (master key).
	- Key `0x00`. Version: `7b`
- AID: `0x01`. 6 access keys.
	- Key `0x00`. Version `7e`
	- Key `0x01`. Version `ffffffc5`
	- Key `0x02`. Version `18`
	- Key `0x03`. Version `70`
	- Key `0x04`. Version `ffffff8b`
	- Key `0x05`. Version `ffffffc6`

### Files

- AID: `0x01`
	- File `0x00`. Backup file.
	- File `0x01`. Backup file.
	- File `0x02`. Backup file.
	- File `0x03`. Backup file.	 
	- File `0x04`. Backup file.	 
	- File `0x05`. Backup file.	 
	- File `0x06`. Backup file.	 
	- File `0x07`. Backup file.	 
	- File `0x08`. Standard file.	 
	- File `0x09`. Standard file.
	- File `0x0A`. Standard file.	 
	- File `0x0B`. Standard file.	 
	 

## Communication

<img src="https://github.com/mgp25/CRTM/blob/master/Assets/communications.png">

### Init connection

**Description:** This requests generates a new session that is set as a Cookie with `JSESSIONID`. Some information related to our device and timezone is being sent in the POST, however what will identify us is `uuid`.

**Request:**

```json
POST /middlelat/midd/device/init/conn HTTP/1.1
Content-Type: application/json; charset=UTF-8
Content-Length: 620
Host: lat1p.crtm.es:39480
Connection: close
Accept-Encoding: gzip, deflate
User-Agent: okhttp/3.12.1

{"board":"MSM8974","bootLoader":"unknown","brand":"oneplus","build":"MTC20F","device":"A0001","display":"MTC20F test-keys","fingerprint":"oneplus/bacon/A0001:6.0.1/MMB29X/ZNH0EAS2JK:user/release-keys","hardware":"bacon","initAt":"Wed Jan 29 21:23:15 GMT+01:00 2020","language":"en","macAddress":"02:00:00:00:00:00","manufacture":"OnePlus","model":"A0001","networkType":0,"osName":"LOLLIPOP_MR1","osVersion":"6.0.1","product":"bacon","radio":"unknown","screenResolution":"1080x1920","serial":"1a5e4ecc","time":0,"timezone":"Europe/Madrid","uuid":"e626f4c4-34aa-4ca2-bf2b-3c8e0e5e7d26b1f3ee2f-6bdc-49f4-af27-f40218c3e3d1"}
```

**Response:**

```
HTTP/1.1 200 OK
Date: Fri, 31 Jan 2020 00:50:01 GMT
Server: Apache/2.2.3 (CentOS)
Set-Cookie: JSESSIONID=0C1B6566468F2B2A1E382371832C2860.worker2; Path=/middlelat; Secure; HttpOnly
Content-Length: 72
Connection: close
Content-Type: text/plain;charset=UTF-8

e626f4c4-34aa-4ca2-bf2b-3c8e0e5e7d26b1f3ee2f-6bdc-49f4-af27-f40218c3e3d1
```

### Check balance

#### GetVersion (0x60)

![](https://mermaid.ink/img/eyJjb2RlIjoic2VxdWVuY2VEaWFncmFtXG4gICAgcGFydGljaXBhbnQgUENEXG4gICAgcGFydGljaXBhbnQgUElDQ1xuICAgIFBDRC0-PlBJQ0M6IDYwXG4gICAgUElDQy0-PlBDRDogQUYwNDAxMDEwMTAwMTgwNVxuICAgIFBDRC0-PlBJQ0M6IEFGXG4gICAgUElDQy0-PlBDRDogQUYwNDAxMDEwMTA0MTgwNVxuICAgIFBDRC0-PlBJQ0M6IEFGXG4gICAgUElDQy0-PlBDRDogMDAwNDgwMkExQUEzNUI4MEI5MEMxNzUxOTA0OTE3IiwibWVybWFpZCI6eyJ0aGVtZSI6ImRlZmF1bHQifSwidXBkYXRlRWRpdG9yIjpmYWxzZX0)

First frame: `AF04010101001805`

| Status | Vendor ID | Type | Subtype | Major Version | Minor Version | Storage Size | Protocol |
|--------|-----------|------|---------|---------------|---------------|--------------|----------|
| AF     | 04        | 01   | 01      | 01            | 00            | 18           | 05       |

- Vendor ID: `0x04` for PHILIPS
- Storage size: `0x18` = 4096 bytes
- Protocol: `0x05` for ISO 14443-2 and -3

Second frame: `AF04010101041805`

| Status | Vendor ID | Type | Subtype | Major Version | Minor Version | Storage Size | Protocol |
|--------|-----------|------|---------|---------------|---------------|--------------|----------|
| AF     | 04        | 01   | 01      | 01            | 04            | 18           | 05       |

- Vendor ID: `0x04` for PHILIPS
- Storage size: `0x18` = 4096 bytes
- Protocol: `0x05` for ISO 14443-3 and -4

Third frame: `0004802A1AA35B80B90C1751904917`

| Status | UID            | Batch no   | cw prod | prod year |
|--------|----------------|------------|---------|-----------|
| 00     | 04802A1AA35B80 | B90C175190 | 49      | 17        |

- UID: 04802A1AA35B80
- Batch No: B90C175190
- Calendar week: 49 (Dec 4. 2017)
- Year: 2017

### Select application (0x5a)

![](https://mermaid.ink/img/eyJjb2RlIjoic2VxdWVuY2VEaWFncmFtXG4gICAgcGFydGljaXBhbnQgUENEXG4gICAgcGFydGljaXBhbnQgUElDQ1xuICAgIFBDRC0-PlBJQ0M6IDVBMDAwMDAxXG4gICAgUElDQy0-PlBDRDogMDAiLCJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJ1cGRhdGVFZGl0b3IiOmZhbHNlfQ)

| CMD | AID     |
|-----|---------|
| 5A  | 0000001 |

### Authenticate (0x0A)

It authenticates with `0x02` key.


In this procedure both, the PICC as  well as the reader device, show in an  encrypted  way that they posses the same secret  which especially means the same key. This procedure not only confirms that both entities can trust each other but also generates a session key which can be used to keep the further communication path  secure.  As  the  name  “session  key”  implicitly  indicates,  each  time  a  new  authentication  procedure is successfully completed a new key for further cryptographic operations is obtained. [3]

![](https://mermaid.ink/img/eyJjb2RlIjoic2VxdWVuY2VEaWFncmFtXG4gICAgcGFydGljaXBhbnQgUENEXG4gICAgcGFydGljaXBhbnQgUElDQ1xuICAgIFBDRC0-PlBJQ0M6IDBBMDJcbiAgICBQSUNDLT4-UENEOiBBRjM4Q0VGNUY1RjY2QjBGQ0NcbiAgICBQQ0QtPj5QSUNDOiBBRjJCOEQ0QzJFODk5MDI0MTI4OTQ0ODI3QUZENDJDOUFDXG4gICAgUElDQy0-PlBDRDogMDA5MUNCQ0MxMTU3QTI4QzhBXG4iLCJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJ1cGRhdGVFZGl0b3IiOmZhbHNlfQ)

<img src="https://raw.githubusercontent.com/CRTM-NFC/Mifare-Desfire/master/Assets/authentication.png">


### ReadData (0xBD)

Files read:

- `0x00`
- `0x01`
- `0x02`
- `0x03`
- `0x04`
- `0x05`
- `0x06`
- `0x07`
- `0x08`
- `0x09`
- `0x0A`

<img src="https://raw.githubusercontent.com/CRTM-NFC/Mifare-Desfire/master/Assets/readdata">

### Authentication (0x0A)

Authentication is performed again with `0x01` key.

### ReadData (0xBD)

Now it reads file `0x0B`

### Update (communication finished)

**Request:** 

```
GET /middlelat/device/front/Update HTTP/1.1
Cookie: JSESSIONID=9ED8374295193A72D1FD4954A1D83C89.worker5; Path=/middlelat; Secure; HttpOnly
Host: lat1p.crtm.es:39480
Connection: close
Accept-Encoding: gzip, deflate
User-Agent: okhttp/3.12.1
```

**Response:**

```
HTTP/1.1 200 OK
Date: Mon, 16 Mar 2020 11:41:04 GMT
Server: Apache/2.2.3 (CentOS)
Content-Length: 11
Connection: close
Content-Type: text/plain;charset=UTF-8

STATUS=00
```

### Show balance

```
GET /middlelat/device/front/MuestraSaldo HTTP/1.1
Cookie: JSESSIONID=9ED8374295193A72D1FD4954A1D83C89.worker5; Path=/middlelat; Secure; HttpOnly
Host: lat1p.crtm.es:39480
Connection: close
Accept-Encoding: gzip, deflate
User-Agent: okhttp/3.12.1
```

```
HTTP/1.1 200 OK
Date: Mon, 16 Mar 2020 11:41:04 GMT
Server: Apache/2.2.3 (CentOS)
Content-Length: 304
Connection: close
Content-Type: text/plain;charset=UTF-8

STATUS=00
NOW=16-03-2020
LOTE=MB
SNLOTE=06677651
NUM=04802A1AA35B80
TTARJETA=04
FIV=29-11-2018
FFV=29-11-2028
APPBLK=false
P1N=Normal
P1ID=01
P1FI=29-11-2018
P1FF=29-11-2028
P2N=Anonimo
P2ID=09
P2FI=29-11-2018
P2FF=29-11-2028
P3N=Turistico Normal
P3ID=0B
P3FI=29-11-2018
P3FF=29-11-2028
NumWarningMsg=0
```

## App

### Constants

- `SalePoint`: `010201000001`
-  `CONST_KEY_AT`: `uGpeE45u5c5AgyULp1Uy5hRHWln92g8a`
- `CONST_KEY_ID`: `ZdPZfPPFYoRT9gcqm965HChS5ojEWjlz`
- `CONST_SHA`: `2L5tE938257tZ63iIb3u7L9NHdvyHW5v`

### Network status values

| Status  | Value  |
|---|---|
| `Servicio LAT detenido al no haberse podido leer correctamente los ficheros de configuración`  | 10 |
| `El OTP suministrado no ha podido ser validado`  | 20 |
| `No es posible aplicar el perfil turístico infantil por no alcanzar la edad mínima`  | 50 |
| `No es posible aplicar el perfil turístico infantil por superar la edad máxima`  | 51 |
| `No es posible aplicar el perfil turístico infantil por superar la fecha de nacimiento el momento actual`  | 52 |
| `El código de tarjeta indicado no figura como disponilble para venta en el fichero ITT`  | 53 |
| `La tarjeta indicada para venta no tiene bloqueada la aplicacion en el TLV70 del Feap`  | 54 |
| `No existe tarifa en el fichero TLP para el titulo indicado`  | 55 |
| `Operación incompleta no se ha leído`  | 80 |
| `Tarjeta no válida`  | 81 |
| `Tarjeta no auténtica`  | 82 |
| `Tarjeta con aplicación no activa`  | 83 |
| `Tarjeta con aplicación caducada`  | 84 |
| `La tarjeta no tiene la aplicación BIT`  | 85 |
| `La tarjeta no tiene una aplicación BIT con la versión adecuada`  | 86 |
| `La tarjeta no tiene títulos activos`  | 87 |
| `La tarjeta esta en lista negra, operación no permitida`  | 88 |
| `La tarjeta esta en lista negra, se bloquea la aplicación BIT`  | 89 |
| `La tarjeta esta en lista negra, se bloquea el título 1`  | 8A |
| `La tarjeta esta en lista negra, se bloquea el título 2`  | 8B |
| `La tarjeta esta en lista negra, se bloquea el título 3`  | 8C |
| `La tarjeta esta en lista negra, se permite la operación`  | 8D |
| `No se ha suministrado la sesión`  | A0 |
| `Hay sesión pero no tenemos el objeto LAT Manager`  | A1 |
| `La tarjeta no tiene perfiles válidos`  | A7 |
| `La tarjeta no tiene un perfil normal válido`  | A9 |
| `RUF Error`  | B0-BF |
| `El dSalePoint suministrado tiene una longitud incorrecta, deben ser 6 bytes`  | C0 |
| `No se ha suministrado el token JWT en una operación de inspección`  | 00A0 |
| `No se ha podido validar el token JWT necesario para la inspección`  | 00A1 |


More network codes on class: `com.sgcr.vo`.
        
        
## Server 

## Status codes

| Status  | Value  |
|---|---|
| `STATUS_OK`  | 00 |
| `STATUS_OKINCONS`  | 01 |
| `STATUS_SESSION_INVALID`  | A0 |
| `STATUS_TOKEN_EMPTY`  | A1 |
| `STATUS_PENDING`  | AF |
| `STATUS_ERR_LOG`  | EF |
| `STATUS_ERR_OTP`  | FD |
| `STATUS_KO`  | FF |


- `STATUS_OK`: Operación realizada correctamente
- `STATUS_OKINCONS`: Tarjeta BIT contiene alguna inconsistencia`
- `STATUS_SESSION_INVALID`: La sesión no existe o es inválida
- `STATUS_TOKEN_EMPTY`: Token de acceso no proporcionado
- `STATUS_PENDING`: El Proceso se ha interrumpido
- `STATUS_ERR_LOG`: Error lógico. Interfaz / operación
- `STATUS_ERR_OTP`: OTP no suministrado
- `STATUS_KO`: error no controlado

**Note:** `AF` means the systems is pending for new commands.

Within the HTTP requests, `CMD` parameter is used to indicate next command to send to the NFC card, the response obtained from the NFC card is then sent it back to the server via GET request.


- Generate NFC command: `/middlelat/device/front/GeneraComando?respuesta=<VALUE>`

## Endpoints

#### Generate command

**Request:**

```
GET /middlelat/device/front/GeneraComando?respuesta=007EE6FFE255CB7EB8 HTTP/1.1
Cookie: JSESSIONID=5AE30B6DA466C6BEAD94061558300959.worker2; Path=/middlelat; Secure; HttpOnly
Host: lat1p.crtm.es:39480
Connection: close
Accept-Encoding: gzip, deflate
User-Agent: okhttp/3.12.1
```

**Response:**

```
HTTP/1.1 200 OK
Date: Wed, 29 Jan 2020 20:53:03 GMT
Server: Apache/2.2.3 (CentOS)
Content-Length: 50
Connection: close
Content-Type: text/plain;charset=UTF-8

STATUS=AF
CMD=AF402AF9127495F99F60783D3337234B0C
```

## Security researchs

### Side channel attacks

**There is no available information wether someone has achieved  a side channel attack on the MIFARE DESFire EV1 (MF3ICD41)**. However, there was an study made on the MF3ICD40 which was vulnerable using differencial power analysis to leak partial key information and used a template to crack the key [4] [5].


## Custom APK for debugging NFC communication

You can download the apk from here: [https://github.com/CRTM-NFC/Mifare-Desfire/blob/master/App/app-debug.apk](https://github.com/CRTM-NFC/Mifare-Desfire/blob/master/App/app-debug.apk)

<img src="https://raw.githubusercontent.com/CRTM-NFC/Mifare-Desfire/master/Assets/appdebug0.jpg" width=300> <img src="https://raw.githubusercontent.com/CRTM-NFC/Mifare-Desfire/master/Assets/appdebug1.jpg" width=300>


## Authors

- [un1k0n](https://github.com/un1k0n)
- [offk0rs](https://github.com/offk0rs)
- And myself, [mgp25](https://github.com/mgp25)


## References

[1] [https://www.nxp.com/docs/en/data-sheet/MF3ICDX21_41_81_SDS.pdf](https://www.nxp.com/docs/en/data-sheet/MF3ICDX21_41_81_SDS.pdf)

[2] [https://www.mifare.net/mistory/mifare-training-successfully-held-at-metro-de-madrid-premises/](https://www.mifare.net/mistory/mifare-training-successfully-held-at-metro-de-madrid-premises/)

[3] [http://read.pudn.com/downloads165/ebook/753406/M075031_desfire.pdf](http://read.pudn.com/downloads165/ebook/753406/M075031_desfire.pdf)

[4] [https://www.iacr.org/workshops/ches/ches2011/presentations/Session%205/CHES2011_Session5_1.pdf](https://www.iacr.org/workshops/ches/ches2011/presentations/Session%205/CHES2011_Session5_1.pdf)

[5] [https://www.emsec.ruhr-uni-bochum.de/media/crypto/veroeffentlichungen/2011/10/10/desfire_2011_extended_1.pdf](https://www.emsec.ruhr-uni-bochum.de/media/crypto/veroeffentlichungen/2011/10/10/desfire_2011_extended_1.pdf)
