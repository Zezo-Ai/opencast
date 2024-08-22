# Terms of use

Opencast allows you to display terms of use for new users in the admin UI. The user will have to accept the terms to continue using the UI.

To enable terms of use, set the following in `etc/org.opencastproject.organization-mh_default_org.cfg`:

```
prop.org.opencastproject.admin.display_terms=true
```

The content of the terms can be set in `/etc/ui-config/mh_default_org/admin-ui/` per language. The filenames follow these rules:

```
terms.%{language_code}.html
```

The corresponding language codes can be found here: https://github.com/opencast/opencast-admin-interface/blob/admin-ui-picard/app/src/i18n/i18n.ts#L44

If a language is not provided, the english version will be displayed.
