# Georges Arnaout: consulting site

A static site built by GitHub Pages (Jekyll). There is nothing to install: you
edit files, commit, and GitHub rebuilds the site in about a minute.

## Put it online

1. Create a new public repository on GitHub, for example `advisory`.
2. Upload everything in this folder to the repository root (drag the files into
   **Add file > Upload files**, or push with git). `_config.yml` must sit at
   the top level of the repository.
3. In the repository, open **Settings > Pages**. Under **Build and deployment**
   choose **Deploy from a branch**, branch `main`, folder `/ (root)`, and save.
4. After a minute the site is live at `https://garnaout.github.io/advisory/`.

The site works at any address without changes, because GitHub Pages fills in
the address for you. If the page ever loads without its styling, open
`_config.yml` and set `baseurl: "/advisory"` (your repository name).

Two things to know:

- This will not work inside the `new/` folder of your existing
  `garnaout.github.io` repository. GitHub only builds a site whose
  `_config.yml` is at the repository root, so use a separate repository.
- To use your own domain, add it under **Settings > Pages > Custom domain** and
  point the domain's DNS at GitHub as that page describes.

## Write a blog post

1. Copy `POST-TEMPLATE.md` into the `_posts` folder.
2. Rename it `YYYY-MM-DD-your-title.md`, for example
   `2026-10-15-what-a-health-score-is-for.md`. The date is the publish date and
   the rest becomes the web address.
3. Fill in the title, the one-line description and the text, then commit.

You can do all of this in the browser: open `_posts` on GitHub and choose
**Add file > Create new file**. A post dated in the future stays hidden until
that date. The newest three posts appear on the home page, all of them on
`/blog/`, and readers can subscribe at `/feed.xml`.

## Change what the site says

| To change | Edit |
| --- | --- |
| Headline, About text, ways to work together | `index.html` |
| The problems under "What I get asked to fix" | `_data/advisory.yml` |
| The track record table | `_data/track_record.yml` |
| Talks | `_data/talks.yml` |
| Testimonials (hidden until you add one) | `_data/testimonials.yml` |
| Research and publications | `research/index.md` |
| Contact email, LinkedIn link, your photo | `_config.yml` |
| Colors and type | the top of `assets/css/main.css` |

### Add your photo

Upload a square photo to `assets/img/`, then set `photo` in `_config.yml`:

```yaml
photo: /assets/img/georges-arnaout.jpg
```

Until then the About section shows a monogram.

### Show an email address

Set `email` in `_config.yml`. It then appears in the contact section and the
footer. While it is empty, the site points people to LinkedIn.

## Preview on your own computer (optional)

You need Ruby. Then, in this folder:

```
bundle install
bundle exec jekyll serve
```

and open `http://localhost:4000`.
