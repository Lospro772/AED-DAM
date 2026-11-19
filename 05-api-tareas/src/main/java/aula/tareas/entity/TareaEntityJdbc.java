package aula.tareas.entity;

public class TareaEntityJdbc {

    private Long id;
    private String titulo;

    public TareaEntityJdbc(Long id) {
        this.id = id;
    }

    public TareaEntityJdbc(String titulo, Long id) {
        this.titulo = titulo;
        this.id = id;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

}
