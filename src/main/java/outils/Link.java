package outils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;

@AllArgsConstructor
@Getter
@Setter
public class Link {

    private String rel;
    private String httpMethod;
    private URI uri;
}
