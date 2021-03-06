SELECT *
FROM (SELECT
        base.age_group                                                AS base_age_group,
        base.sort_order                                               AS base_sort_order,
        base.value_reference                                          AS visit_type,
        IF(shortName.name IS NULL, base.concept_name, shortName.name) AS concept_name,
        final.concept_id,
        final.female,
        final.male,
        final.other,
        final.age_group,
        final.sort_order,
        final.male + final.other + final.female                       AS total
      FROM
        (SELECT
           rag.name AS age_group,
           cn.concept_id,
           cn.name  AS concept_name,
           rag.sort_order,
           va.value_reference
         FROM reporting_age_group rag, concept_name cn, (SELECT DISTINCT value_reference
                                                         FROM visit_attribute va
                                                         WHERE va.visit_attribute_id IS NOT NULL #visitType#) va
         WHERE rag.report_group_name = '#ageGroupName#' AND cn.name IN (#conceptNames#) AND
               cn.concept_name_type = 'FULLY_SPECIFIED') base
        LEFT OUTER JOIN
        (SELECT
           concept_name,
           concept_id,
           age_group,
           visit_type,
           sum(female) AS female,
           sum(male)   AS male,
           sum(other)  AS other,
           sort_order
         FROM (SELECT
                 cn.name                                         AS concept_name,
                 cn.concept_id                                   AS concept_id,
                 #countOncePerPatient#(IF(p.gender = 'F', 1, 0)) AS female,
                 #countOncePerPatient#(IF(p.gender = 'M', 1, 0)) AS male,
                 #countOncePerPatient#(IF(p.gender = 'O', 1, 0)) AS other,
                 va.value_reference                              AS visit_type,
                 rag.name                                        AS age_group,
                 p.person_id                                     AS person_id,
                 rag.sort_order                                  AS sort_order
               FROM obs obs
                 INNER JOIN concept_name cn
                   ON obs.concept_id = cn.concept_id AND cn.concept_name_type = 'FULLY_SPECIFIED' AND obs.voided = 0 AND
                      cn.voided = 0
                 INNER JOIN encounter e ON obs.encounter_id = e.encounter_id
                 INNER JOIN visit v ON v.visit_id = e.visit_id
                 INNER JOIN visit_attribute va ON va.visit_id = v.visit_id
                 INNER JOIN visit_attribute_type vat
                   ON vat.visit_attribute_type_id = va.attribute_type_id AND vat.name = 'Visit Status'
                 INNER JOIN person p ON p.person_id = obs.person_id
                 INNER JOIN reporting_age_group rag ON DATE(#endDateField#) BETWEEN (DATE_ADD(
                     DATE_ADD(birthdate, INTERVAL rag.min_years YEAR), INTERVAL rag.min_days DAY)) AND (DATE_ADD(
                     DATE_ADD(birthdate, INTERVAL rag.max_years YEAR), INTERVAL rag.max_days DAY))
                                                       AND rag.report_group_name = '#ageGroupName#'
               WHERE
                 cn.name IN (#conceptNames#) AND cast(#endDateField# AS DATE) BETWEEN '#startDate#' AND '#endDate#' AND
                 obs.voided IS FALSE
               GROUP BY cn.name, cn.concept_id, va.value_reference, rag.name, rag.sort_order, p.person_id) result
         GROUP BY concept_name, age_group, visit_type
         ORDER BY concept_name, result.sort_order) final
          ON final.age_group = base.age_group AND final.concept_id = base.concept_id AND
             final.visit_type = base.value_reference
        LEFT OUTER JOIN concept_name shortName
          ON shortName.concept_id = base.concept_id AND shortName.concept_name_type = 'SHORT' AND shortName.voided = 0
      ORDER BY base.sort_order ASC) AS result;