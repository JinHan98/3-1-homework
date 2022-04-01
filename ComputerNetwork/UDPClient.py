from http import server
from pydoc import cli
from socket import*
serverName='my_ip'
serverPort=12000
clientSocket=socket(AF_INET,SOCK_DGRAM)
m=input('Input lowercase sentence:')
clientSocket.sendto(m.encode(),(serverName,serverPort))
modifiedMessage, serverAddress =clientSocket.recvfrom(2048)
print(modifiedMessage.decode())
clientSocket.close