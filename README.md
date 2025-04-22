# Reti-Di-Calcolatori-RDC
## Socket UDP java
### Client
**Init**
```
    DatagramPacket dp = null;
    DatagramSocket ds = null;
    byte[] buf = new byte[256];
    ds = new DatagramSocket();
    ds.setSoTimeout(30000);
    dp = new DatagramPacket(buf, buf.length, Saddr, port);
```
**Create Data Transfer Stream**
```
    ByteArrayOutputStream bos = null;
    DataOutputStream dos = null;
    ByteArrayInputStream bis = null;
    DataInputStream dis = null;


    bis = new ByteArrayInputStream(dp.getData());
    dis = new DataInputStream(bis);
```
**To Server**
```
    bos = new ByteArrayOutputStream();
    dos = new DataOutputStream(bos);
    dos.writeUTF(request);
    dp.setData(bos.toByteArray());
    ds.send(dp);
```
**From Server**
```
// clean buf
    dp.setData(buf);
    ds.receive(dp);
    bis = new ByteArrayInputStream(dp.getData());
    dis = new DataInputStream(bis);
    response = dis.readUTF();
```
### Server
`DatagramSocket ds`(`DatagramPacket dp`) : `ds.send(dp)`,`ds.receive(dp)`

*output* Data -> `DataOutputStream` (`ByteArrayOutputStream`) -> ByteArray 

*input* ByteArray -> `ByteArrayInputStream` (`DataInputStream`) -> Data

## Socket TCP java
### Client
### Server
`Socket socket = new Socket(addr, port);`
Create input stream
`insock = new DataInputStream(socket.getInputStream());`
Create output stream
`outsock = new DataOutputStream(socket.getOutputStream());`

## Socket UDP C
### Client
### Server
`sd = socket(AF_INET, SOCK_DGRAM, 0);`

## Socket TCP C
### Client
### Server