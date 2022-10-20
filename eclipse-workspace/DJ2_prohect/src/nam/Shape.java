package nam;

public abstract class Shape {
    private Shape next;

    public Shape(){
        next = null;
    }

    public void setNext(Shape next) {
        this.next = next;
    }

    public Shape getNext(){
        return next;
    }

    public abstract void draw();
}
