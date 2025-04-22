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


## Socket TCP java
### Client :
### Server :


## Socket UDP C
### Client :
### Server :


## Socket TCP C
### Client :
### Server :