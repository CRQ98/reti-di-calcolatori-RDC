# Reti-Di-Calcolatori-RDC
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

## Socket TCP java
### Client :
### Server :


## Socket UDP C
### Client :
### Server :


## Socket TCP C
### Client :
### Server :

## RMI
## RPC
#1 open
