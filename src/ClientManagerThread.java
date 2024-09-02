import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

public class ClientManagerThread extends Thread {
  private Socket m_socket;
  private int m_clientIndex; // 클라이언트의 인덱스
  private AtomicLong lastActivityTime; // 클라이언트의 마지막 활동 시간을 저장
  private static final long MAX_INACTIVITY_TIMEOUT = 1000; // 클라이언트의 최대 비활동 시간 (60초)

  public ClientManagerThread(int clientIndex) {
    this.m_clientIndex = clientIndex;
    lastActivityTime = new AtomicLong(System.currentTimeMillis());
  }

  @Override
  public void run() {
    super.run();
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
      String text;

      while (true) {
        text = in.readLine();
        if (text != null) {
          // 클라이언트의 인덱스와 메시지를 함께 보내기
          String messageWithIndex = m_clientIndex + ":" + text;
          broadcastMessage(messageWithIndex);
          // 클라이언트 활동이 있었으므로 시간 업데이트
          lastActivityTime.set(System.currentTimeMillis());
        } else {
          // 클라이언트의 활동이 없을 때
          long currentTime = System.currentTimeMillis();
          if (currentTime - lastActivityTime.get() > MAX_INACTIVITY_TIMEOUT) {
            // 클라이언트의 비활동 시간이 최대 시간을 초과하면 연결 종료
            disconnectClient();
            return;
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      disconnectClient();
    }
  }

  private void broadcastMessage(String message) {
    for (PrintWriter clientWriter : MyServer.m_OutputList) {
      clientWriter.println(message);
      clientWriter.flush();
    }
  }

  private void disconnectClient() {
    System.out.println("클라이언트 연결이 끊어짐: " + m_clientIndex);
//    MyServer.returnClientIndex(m_clientIndex); // 인덱스 반환
    MyServer.removeClient(m_clientIndex);
    // 필요한 경우 추가 정리 또는 로깅을 수행
    // 스레드를 종료하지 않고, 클라이언트 연결 종료 상태를 표시할 수 있습니다.
  }

  public void setSocket(Socket _socket) {
    m_socket = _socket;
  }
}

