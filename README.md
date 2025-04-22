# Reti-Di-Calcolatori-RDC
<ins>Close everything that you dont need any more!!!</ins>
---
<details>
<summary> Socket UDP java </summary>

## Socket UDP java  
### Client :
**Init**
```java
DatagramPacket dp = null;
DatagramSocket ds = null;
byte[] buf = new byte[256];
ds = new DatagramSocket();
ds.setSoTimeout(30000);
dp = new DatagramPacket(buf, buf.length, saddr, port);
```
**Create Data Transfer Stream**
```java
ByteArrayOutputStream bos = null;
DataOutputStream dos = null;
ByteArrayInputStream bis = null;
DataInputStream dis = null;
```
**To Server**
```java
bos = new ByteArrayOutputStream();
dos = new DataOutputStream(bos);
dos.writeUTF(request);
dp.setData(bos.toByteArray());
ds.send(dp);
```
**From Server**
```java
// clean buf
dp.setData(buf);
ds.receive(dp);
bis = new ByteArrayInputStream(dp.getData());
dis = new DataInputStream(bis);
response = dis.readUTF();
```
### Server :
**Init**
```java
DatagramSocket ds = null;
DatagramPacket dp = null;
ds = new DatagramSocket(port);
dp = new DatagramPacket(buf, buf.length);
```
**Create Data Transfer Stream**
```java
ByteArrayOutputStream bos = null;
DataOutputStream dos = null;
ByteArrayInputStream bis = null;
DataInputStream dis = null;
```
**From Client**
```java
bis = new ByteArrayInputStream(dp.getData());
dis = new DataInputStream(bis);
request = dis.readUTF();
```
**To Client**
```java
bos = new ByteArrayOutputStream();
dos = new DataOutputStream(bos);
dos.writeUTF(respose);
dp.setData(bos.toByteArray());
ds.send(dp);
```
</details>

<details>
<summary> Socket TCP java </summary>

## Socket TCP java
### Client :
**Init**
```java
Socket socket = null;
socket = new Socket(addr, port);
socket.setSoTimeout(20000);
```
**Get sock streams**
```java
DataInputStream insock = null;
DataOutputStream outsock = null;
insock = new DataInputStream(socket.getInputStream());
outsock = new DataOutputStream(socket.getOutputStream());
```
**To Server**
```java
outsock.writeUTF(filename);
My.transferFileBinary(infile, outsock);
```
**From Server**
```java
outcome = insock.readUTF();
```
### Server :
</details>


<details>
<summary> Socket UDP C </summary>

## Socket UDP C
### Client :
### Server :
</details>

<details>
<summary> Socket TCP C </summary>

## Socket TCP C
### Client :
### Server :
</details>

<details>
<summary> RMI </summary>

## RMI
</details>
<details>
<summary> RPC </summary>

## RPC
</details>

