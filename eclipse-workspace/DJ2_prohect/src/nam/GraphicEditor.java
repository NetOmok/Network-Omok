package nam;

import java.util.Scanner;

public class GraphicEditor {
    private Shape head; //연결리스트 헤더 포인터
    Scanner s = new Scanner(System.in);

    public GraphicEditor(){
        head = null;

        System.out.println("그래픽 에디터 beauty를 실행합니다.");

        while(true){
            System.out.print("삽입(1), 삭제(2), 모두 보기(3), 종료(4) >> ");
            int opt = s.nextInt();

            if(opt == 4){
                System.out.println("그래픽 에디터를 종료합니다.");
                break;
            }

            switch (opt){
                case 1:
                    System.out.print("Line(1), Rect(2), Circle(3) >> ");
                    int n = s.nextInt();

                    if(n == 1) insert(new Line());
                    else if(n == 2) insert(new Rect());
                    else if(n == 3) insert(new Circle());
                    break;

                case 2:
                    System.out.print("삭제하고자 하는 도형 >> ");
                    delete(s.nextInt());
                    break;

                case 3:
                    show();
                    break;
            }

        }


    }

    public void insert(Shape obj){
        Shape shape = head;

        if (shape == null) // 연결리스트가 공백 리스트인 경우
            head = obj;
        else{
            while(shape.getNext() != null){ // 맨 끝 노드로 이동
                shape = shape.getNext();
            }
            shape.setNext(obj); // 맨끝에 삽입
        }
    }

    public void delete(int n){
        Shape parent = null; // 삭제하고자 하는 노드의 앞 노드
        Shape shape = head; // 삭제하고자 하는 노드

        try{
            if(n == 1){ // 첫번째 노드를 삭제하는 경우
                head = head.getNext();
            }
            else{ // 중간 또는 맨뒤 노드를 삭제하는 경우
                for(int i=1; i<n; i++){ // 삭제하고자 하는 노드까지 이동
                    parent = shape;
                    shape = shape.getNext();
                } // 존재하지 않는 노드로 이동시엔 예외 발생

                parent.setNext(shape.getNext()); // 삭제하는 노드 다음 노드를 앞 노드에 붙여줌
            }
        }catch (Exception e){ // 예외처리로 삭제할 수 없는 경우는 메시지 출력
            System.out.println("삭제할 수 없습니다.");
        }
    }

    public void show(){
        Shape shape = head;

        while(shape != null){
            shape.draw(); // 각 도형에 맞는 draw 함수 호출
            shape = shape.getNext();
        }

    }


}
