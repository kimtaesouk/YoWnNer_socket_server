import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyServer {
  private static final int MAX_NUM_CLIENTS = 1000;
  private static List<Integer> availableClientIndices = new ArrayList<>();
  public static ArrayList<PrintWriter> m_OutputList;
  private static int clientCounter = 0;

  public static void main(String[] args) {
    m_OutputList = new ArrayList<PrintWriter>();
    for (int i = 0; i < MAX_NUM_CLIENTS; i++) {
      availableClientIndices.add(i);
    }

    try {
      ServerSocket s_socket = new ServerSocket(5002);
      InetAddress localAddress = InetAddress.getLocalHost();
      System.out.println("서버가 5002 포트에서 열렸습니다.");
      System.out.println("현재 서버의 IP 주소: " + localAddress.getHostAddress());
      while (true) {
        Socket c_socket = s_socket.accept();
        ClientManagerThread c_thread = new ClientManagerThread(clientCounter);
        c_thread.setSocket(c_socket);

        // 클라이언트의 IP 주소를 출력
        String clientIP = c_socket.getInetAddress().getHostAddress();
        System.out.println("새로운 클라이언트가 연결되었습니다. IP 주소: " + clientIP);

        int smallestIndex = Collections.min(availableClientIndices);

        System.out.println("가장 작음: " + smallestIndex);

        m_OutputList.add(smallestIndex, new PrintWriter(c_socket.getOutputStream()));
        System.out.println("현재 연결된 클라이언트 수: " + m_OutputList.size());
        c_thread.start();
        clientCounter++;
        availableClientIndices.remove(Integer.valueOf(smallestIndex));
        System.out.println(clientCounter);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("00");
    }
  }

  public static void returnClientIndex(int index) {
    // 클라이언트 인덱스를 사용 가능한 목록에 추가
    availableClientIndices.add(index);
  }

  // 클라이언트를 제거하는 메소드
  public static void removeClient(int clientIndex) {
    if (clientCounter > 0) {
      m_OutputList.remove(clientIndex);
      clientCounter--;

      // 빈칸이 생겼으므로 사용 가능한 목록에 해당 인덱스를 추가
      availableClientIndices.add(clientIndex);

      // 클라이언트 인덱스 재조정
      for (int i = clientIndex; i < m_OutputList.size(); i++) {
        m_OutputList.get(i).println("index:" + i); // 선택적으로 인덱스를 클라이언트에 보낼 수 있습니다.
      }

      System.out.println(m_OutputList);
    }
  }
}
