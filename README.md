# Reti-Di-Calcolatori-RDC
## Socket UDP java
# Client


# Server
`DatagramSocket ds`(`DatagramPacket dp`) : `ds.send(dp)`,`ds.receive(dp)`

*output* Data -> `DataOutputStream` (`ByteArrayOutputStream`) -> ByteArray 

*input* ByteArray -> `ByteArrayInputStream` (`DataInputStream`) -> Data

## Socket TCP java
# Client
# Server
`Socket socket = new Socket(addr, port);`
Create input stream
`insock = new DataInputStream(socket.getInputStream());`
Create output stream
`outsock = new DataOutputStream(socket.getOutputStream());`

## Socket UDP C
# Client
# Server
`sd = socket(AF_INET, SOCK_DGRAM, 0);`

## Socket TCP C
# Client
# Server