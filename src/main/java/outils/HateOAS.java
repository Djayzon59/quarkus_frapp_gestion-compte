package outils;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
@Getter
@Setter
public class HateOAS implements Serializable {

    private String message;
    private ArrayList<Link> listeLinks;

    public HateOAS() {
        listeLinks = new ArrayList<>();
    }

    public void addLink(Link link){
        listeLinks.add(link);
    }
}
